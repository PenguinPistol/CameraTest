package me.penguinpistol.cameratest;

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
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import me.penguinpistol.cameratest.camera.FaceDirection;
import me.penguinpistol.cameratest.camera.PhotoType;
import me.penguinpistol.cameratest.camera.SideFaceChecker;

public class TakePictureViewModel extends ViewModel {
    public static final String KEY_SIDE_TYPE = "SIDE_TYPE";
    public static final String KEY_SIDE_PHOTO_LIST = "SIDE_PHOTO_LIST";

    private final Map<FaceDirection, Uri> takePhotoMap;
    private NavController navController;

    public TakePictureViewModel() {
        this.takePhotoMap = new HashMap<>();
    }

    public void setNavController(NavController navController) {
        this.navController = navController;
    }

    public NavController getNavController() {
        return navController;
    }

    public void setTakePhoto(FaceDirection direction, Uri photoUri) {
        takePhotoMap.put(direction, photoUri);
    }

    @NonNull
    public Map<FaceDirection, Uri> getTakePhotoMap() {
        return takePhotoMap;
    }

    public boolean removeFile(Uri uri) {
        if(uri != null) {
            File target = new File(uri.getPath());
            if (target.exists()) {
                return target.delete();
            }
        }
        return false;
    }

    public void startSidePhotoAnalysis(@NonNull Context context, PhotoType photoType, List<Uri> photoList, BiConsumer<ArrayList<Uri>, ArrayList<Uri>> onComplete) {
        Executors.newSingleThreadExecutor().execute(() -> {
            FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                    .build();

            FaceDetector faceDetector = FaceDetection.getClient(options);
            SideFaceChecker faceChecker = new SideFaceChecker();

            final ArrayList<Uri> photoList30 = new ArrayList<>();
            final ArrayList<Uri> photoList45 = new ArrayList<>();
            int[] completeCount = new int[] {0};

            for(Uri uri : photoList) {
                try {
                    InputImage inputImage = InputImage.fromFilePath(context, uri);
                    faceDetector.process(inputImage)
                            .addOnSuccessListener(faces -> {
                                if(faces.size() == 0) {
                                    return;
                                }
                                Face face = faces.get(0);

                                faceChecker.setFaceDirection(photoType == PhotoType.LEFT ? FaceDirection.LEFT_45 : FaceDirection.RIGHT_45);
                                if(faceChecker.check(face)) {
                                    photoList45.add(uri);
                                } else {
                                    faceChecker.setFaceDirection(photoType == PhotoType.LEFT ? FaceDirection.LEFT_30 : FaceDirection.RIGHT_30);
                                    if(faceChecker.check(face)) {
                                        photoList30.add(uri);
                                    } else {
                                        Log.d("CameraXHelper", "Not Match Face");
                                    }
                                }
                                completeCount[0] += 1;
                                Log.d("CameraXHelper", "count >> " + completeCount[0] + " / " + photoList.size());
                                if(completeCount[0] == photoList.size()) {
                                    HandlerCompat.createAsync(Looper.getMainLooper()).post(() -> onComplete.accept(photoList30, photoList45));
                                }
                            })
                            .addOnFailureListener(e -> {
                                e.printStackTrace();
                                completeCount[0] += 1;
                                Log.d("CameraXHelper", "count >> " + completeCount[0] + " / " + photoList.size());
                                if(completeCount[0] == photoList.size()) {
                                    HandlerCompat.createAsync(Looper.getMainLooper()).post(() -> onComplete.accept(photoList30, photoList45));
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
