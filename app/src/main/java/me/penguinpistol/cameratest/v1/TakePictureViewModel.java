package me.penguinpistol.cameratest.v1;

import android.content.Context;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.os.HandlerCompat;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import me.penguinpistol.cameratest.common.AnalysisParameter;
import me.penguinpistol.cameratest.v1.camera.FaceDirection;
import me.penguinpistol.cameratest.v1.camera.PhotoType;
import me.penguinpistol.cameratest.v1.camera.SideFaceChecker;

public class TakePictureViewModel extends ViewModel {
    public static final String KEY_SIDE_TYPE = "SIDE_TYPE";

    private final Map<PhotoType, List<Uri>> takePhotoMap;
    private NavController navController;

    private AnalysisParameter analysisParameter;

    public TakePictureViewModel() {
        this.takePhotoMap = new HashMap<>();
        this.analysisParameter = new AnalysisParameter();
    }

    public void setNavController(NavController navController) {
        this.navController = navController;
    }

    public NavController getNavController() {
        return navController;
    }

    public void clearTakePhoto(PhotoType photoType) {
        if (takePhotoMap.containsKey(photoType)) {
            List<Uri> temp = takePhotoMap.get(photoType);
            if(temp != null) {
                for (Uri uri : temp) {
                    removeFile(uri);
                }
            }
            takePhotoMap.remove(photoType);
        }
    }

    public void setTakePhoto(PhotoType photoType, List<Uri> photos) {
        takePhotoMap.put(photoType, photos);
    }

    @NonNull
    public Map<PhotoType, List<Uri>> getTakePhotoMap() {
        return takePhotoMap;
    }

    public void removeFile(Uri uri) {
        if(uri != null) {
            File target = new File(uri.getPath());
            if (target.exists()) {
                if(!target.delete()) {
                    Log.w("CameraXHelper", "file delete failed! >> " + uri.getPath());
                }
            }
        }
    }

    public void startSidePhotoAnalysis(@NonNull Context context, PhotoType photoType, BiConsumer<ArrayList<Uri>, ArrayList<Uri>> onComplete) {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .build();

        FaceDetector faceDetector = FaceDetection.getClient(options);
        SideFaceChecker faceChecker = new SideFaceChecker();

        final List<Uri> copyList = new CopyOnWriteArrayList<>(takePhotoMap.get(photoType));

        Executors.newSingleThreadExecutor().execute(() -> {
            final ArrayList<Uri> photoList30 = new ArrayList<>();
            final ArrayList<Uri> photoList45 = new ArrayList<>();
            int[] completeCount = new int[] {0};

            for(Uri uri : copyList) {
                try {
                    InputImage inputImage = InputImage.fromFilePath(context, uri);
                    faceDetector.process(inputImage)
                            .addOnSuccessListener(faces -> {
                                if(faces.size() == 0) {
                                    return;
                                }
                                Face face = faces.get(0);
                                Log.d("CameraXHelper", "----------------------------------------");

                                // 저장된 사진파일의 경우 좌우반전 처리되어있어서 계산 기준값을 바꿔야함..
                                faceChecker.setFaceDirection(photoType == PhotoType.LEFT ? FaceDirection.RIGHT_30 : FaceDirection.LEFT_30);
                                if(faceChecker.check(face)) {
                                    photoList30.add(uri);
                                } else {
                                    faceChecker.setFaceDirection(photoType == PhotoType.LEFT ? FaceDirection.RIGHT_45 : FaceDirection.LEFT_45);
                                    if(faceChecker.check(face)) {
                                        photoList45.add(uri);
                                    }
                                }
                            })
                            .addOnCompleteListener(task -> {
                                completeCount[0] += 1;
                                Log.d("CameraXHelper", "count >> " + completeCount[0] + " / " + copyList.size());
                                if(completeCount[0] == copyList.size()) {
                                    HandlerCompat.createAsync(Looper.getMainLooper()).post(() -> onComplete.accept(photoList30, photoList45));
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setParameterImage(FaceDirection direction, Uri uri) {
        analysisParameter.setImage(direction, uri);
    }

    public AnalysisParameter getAnalysisParameter() {
        return analysisParameter;
    }
}
