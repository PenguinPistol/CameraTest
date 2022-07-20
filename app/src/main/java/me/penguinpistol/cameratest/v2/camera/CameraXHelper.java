package me.penguinpistol.cameratest.v2.camera;

import android.net.Uri;
import android.os.Looper;
import android.util.Log;
import android.util.Size;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.os.HandlerCompat;
import androidx.core.util.Consumer;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CameraXHelper {
    private static final Size IMAGE_SIZE = new Size(360, 640);
    private static final Size CAPTURE_SIZE = new Size(720, 1280);
    private static final String FILE_NAME_FORMAT    = "yyyyMMddHHmmssSSS";
    private static final int TAKE_PICTURE_TIME      = 10000;                    // 자동촬영 시간(10s)
    private static final int TAKE_PICTURE_DELAY     = 500;                      // 자동촬영 시작 딜레이(0.5s)
    private static final int TAKE_PICTURE_PERIOD    = 250;                      // 자동촬영 간격(0.25s)

    private ImageCapture mImageCapture;

    private ScheduledExecutorService mTakePictureScheduler;
    private ScheduledExecutorService mCompleteScheduler;

    private final ComponentActivity mActivity;
    private final Executor mMainExecutor;
    private final PreviewView mPreviewView;
    private final List<Uri> mProcessResult;

    public CameraXHelper(@NonNull ComponentActivity activity, @NonNull PreviewView previewView) {
        this.mActivity = activity;
        this.mMainExecutor = ContextCompat.getMainExecutor(activity);
        this.mPreviewView = previewView;
        this.mProcessResult = new ArrayList<>();
    }

    public void startCamera(@NonNull Runnable onCameraStartCallback) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(mActivity);
        cameraProviderFuture.addListener(() -> {
            try {
                Preview preview = new Preview.Builder()
                        .setTargetResolution(IMAGE_SIZE)
                        .build();
                preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());

                mImageCapture = new ImageCapture.Builder()
                        .setTargetResolution(CAPTURE_SIZE)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(mActivity, cameraSelector, preview, mImageCapture);

                onCameraStartCallback.run();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, mMainExecutor);
    }

    public void startProcess(Consumer<List<Uri>> onCompleteCallback) {
        mProcessResult.clear();

        shutdownProcess();

        mTakePictureScheduler = Executors.newSingleThreadScheduledExecutor();
        mTakePictureScheduler.scheduleAtFixedRate(this::takePicture, TAKE_PICTURE_DELAY, TAKE_PICTURE_PERIOD, TimeUnit.MILLISECONDS);

        mCompleteScheduler = Executors.newSingleThreadScheduledExecutor();
        mCompleteScheduler.schedule(() -> {
            if(mTakePictureScheduler != null && !mTakePictureScheduler.isShutdown()) {
                mTakePictureScheduler.shutdown();
                mTakePictureScheduler = null;

                HandlerCompat.createAsync(Looper.getMainLooper()).post(() -> {
                    // Side Complete
                    if(onCompleteCallback != null) {
                        onCompleteCallback.accept(mProcessResult);
                    }
                });
            }
        }, TAKE_PICTURE_TIME + TAKE_PICTURE_DELAY, TimeUnit.MILLISECONDS);
    }

    public void shutdownProcess() {
        if(mTakePictureScheduler != null && !mTakePictureScheduler.isShutdown()) {
            mTakePictureScheduler.shutdown();
        }
        if(mCompleteScheduler != null && !mCompleteScheduler.isShutdown()) {
            mCompleteScheduler.shutdown();
        }

        // 저장한 이미지 전체 삭제
        for(Uri uri : mProcessResult) {
            if(uri != null) {
                File target = new File(uri.getPath());
                if (target.exists()) {
                    if(!target.delete()) {
                        Log.w("CameraXHelper", "file delete failed! >> " + uri.getPath());
                    }
                }
            }
        }
    }

    private void takePicture() {
        if(mImageCapture == null) {
            Log.w("CameraXHelper", "takePicture: image capture is null");
            return;
        }

        String fileName = new SimpleDateFormat(FILE_NAME_FORMAT, Locale.getDefault()).format(System.currentTimeMillis());
        File file = new File(mActivity.getFilesDir().getAbsolutePath() + File.separator + fileName);

        // 이미지 저장시 좌우반전 처리
        ImageCapture.Metadata metadata = new ImageCapture.Metadata();
        metadata.setReversedHorizontal(true);

        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(file)
                .setMetadata(metadata)
                .build();

        mImageCapture.takePicture(
                options,
                mMainExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        mProcessResult.add(outputFileResults.getSavedUri());
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        e.printStackTrace();
                    }
                });
    }
}
