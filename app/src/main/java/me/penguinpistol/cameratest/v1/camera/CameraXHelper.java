package me.penguinpistol.cameratest.v1.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.ViewGroup;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.os.HandlerCompat;
import androidx.core.util.Consumer;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import me.penguinpistol.cameratest.R;

public class CameraXHelper {
    private static final Size CAPTURE_SIZE = new Size(720, 1280);
    private static final Size IMAGE_SIZE = new Size(720, 1280);

    private static final String FILE_NAME_FORMAT    = "yyyyMMddHHmmssSSS";
    private static final int TAKE_PICTURE_TIME      = 5000;     // 자동촬영 시간
    private static final int TAKE_PICTURE_DELAY     = 500;      // 자동촬영 시작 딜레이
    private static final int TAKE_PICTURE_PERIOD    = 100;      // 자동촬영 간격

    private PreviewView mPreviewView;
    private ImageCapture mImageCapture;
    private ImageAnalysis mImageAnalysis;

    private FaceDetectionAnalyzer mAnalyzer;
    private Runnable mOnCameraStartCallback;
    private Runnable mOnDetected;
    private Consumer<Uri> mOnTakePictureCallback;

    // front
    private ExecutorService mAnalyzerExecutor;

    // side
    private ScheduledExecutorService mTakePictureScheduler;
    private ScheduledExecutorService mCompleteScheduler;        // 종료

    private final ComponentActivity mActivity;
    private final Executor mMainExecutor;

    public CameraXHelper(@NonNull ComponentActivity activity, @NonNull ViewGroup container, Runnable callback) {
        mActivity = activity;
        mMainExecutor = ContextCompat.getMainExecutor(activity);
        mOnCameraStartCallback = callback;

        GraphicOverlay graphicOverlay = container.findViewById(R.id.graphic_overlay);
        mPreviewView = container.findViewById(R.id.preview);

        mAnalyzer = new FaceDetectionAnalyzer(graphicOverlay, new FaceDetectionListener() {
            @Override
            public void onDetected(FaceDirection direction) {
                mImageAnalysis.clearAnalyzer();
                mOnDetected.run();
            }

            @Override
            public void onFailure(DetectionFailure reason) {
//                Log.d("CameraXHelper", reason.message);
                graphicOverlay.setFailure(reason);
            }
        });
        mAnalyzer.setImageSize(IMAGE_SIZE);
    }

    public void startCamera() {
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
                mImageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(IMAGE_SIZE)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                cameraProvider.bindToLifecycle(mActivity, cameraSelector, preview, mImageAnalysis, mImageCapture);

                mOnCameraStartCallback.run();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, mMainExecutor);
    }

    public void setOnTakePictureCallback(Consumer<Uri> callback) {
        mOnTakePictureCallback = callback;
    }

    public void startAnalysis(Runnable onDetected) {
        mOnDetected = onDetected;

        if(mAnalyzerExecutor != null && !mAnalyzerExecutor.isShutdown()) {
            mAnalyzerExecutor.shutdown();
            mAnalyzerExecutor = null;
        }
        mAnalyzerExecutor = Executors.newSingleThreadExecutor();
        mImageAnalysis.setAnalyzer(mAnalyzerExecutor, mAnalyzer);
        mAnalyzer.startAnalysis(FaceDirection.FRONT);
    }

    public void startProcess(Runnable onCompleteCallback) {
        if(mTakePictureScheduler != null && !mTakePictureScheduler.isShutdown()) {
            mTakePictureScheduler.shutdown();
        }

        if(mCompleteScheduler != null && !mCompleteScheduler.isShutdown()) {
            mCompleteScheduler.shutdown();
        }

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
                       onCompleteCallback.run();
                   }
                });
            }
        }, TAKE_PICTURE_TIME + TAKE_PICTURE_DELAY, TimeUnit.MILLISECONDS);
    }

    public void takePicture() {
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
                        if(mOnTakePictureCallback != null) {
                            mOnTakePictureCallback.accept(outputFileResults.getSavedUri());
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                    }
                });
    }

    private Bitmap convertBitmap(ImageProxy imageProxy) {
        ByteBuffer byteBuffer = imageProxy.getPlanes()[0].getBuffer();
        byteBuffer.rewind();
        byte[] bytes = new byte[byteBuffer.capacity()];
        byteBuffer.get(bytes);
        byte[] clonedBytes = bytes.clone();
        Bitmap result = BitmapFactory.decodeByteArray(clonedBytes, 0, clonedBytes.length);

        int degrees = imageProxy.getImageInfo().getRotationDegrees();
        if(degrees > 0) {
            // 이미지 회전처리
            Matrix matrix = new Matrix();
            matrix.setScale(-1, 1);
            matrix.postRotate(360 - degrees);
            result = Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, true);
        }

        return result;
    }
}
