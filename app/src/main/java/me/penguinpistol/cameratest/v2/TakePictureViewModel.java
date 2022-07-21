package me.penguinpistol.cameratest.v2;

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

import me.penguinpistol.cameratest.v2.camera.FaceChecker;
import me.penguinpistol.cameratest.v2.camera.FaceDirection;

public class TakePictureViewModel extends ViewModel {

    private NavController navController;
    private List<Uri> processResult;
    private Map<FaceDirection, List<Uri>> analysisResult;
    private AnalysisParameter analysisParameter = new AnalysisParameter();

    public void setAnalysisParameter(FaceDirection dir, Uri uri) {
        analysisParameter.setImage(dir, uri);
    }

    public AnalysisParameter getAnalysisParameter() {
        return analysisParameter;
    }

    public NavController getNavController() {
        return navController;
    }

    public void setNavController(NavController navController) {
        this.navController = navController;
    }

    public List<Uri> getProcessResult() {
        return processResult == null ? new ArrayList<>() : processResult;
    }

    public void setProcessResult(List<Uri> processResult) {
        this.processResult = processResult;
    }

    public void removeAllPhoto() {
        if(processResult != null) {
            for (Uri uri : processResult) {
                removeFile(uri);
            }
        }
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

    public void startAnalysis(@NonNull Context context, Runnable onComplete) {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .build();

        FaceDetector faceDetector = FaceDetection.getClient(options);
        FaceChecker faceChecker = new FaceChecker();
        List<Uri> copyList = new CopyOnWriteArrayList<>(processResult);

        analysisResult = new HashMap<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            int[] completeCount = new int[] {0};

            for(Uri uri : copyList) {
                try {
                    InputImage inputImage = InputImage.fromFilePath(context, uri);
                    faceDetector.process(inputImage)
                        .addOnCompleteListener(task -> {
                            Log.d("TakePicture", "========================================");
                            if(task.isSuccessful()) {
                                List<Face> faces = task.getResult();
                                if(faces.size() == 0) {
                                    Log.d("TakePicture", "face not found!!");
                                } else {
                                    FaceDirection dir = faceChecker.checkDirection(faces.get(0));
                                    if(dir != null) {
                                        List<Uri> list = analysisResult.get(dir);
                                        if(list == null) {
                                            list = new ArrayList<>();
                                            list.add(uri);
                                            analysisResult.put(dir, list);
                                        } else {
                                            list.add(uri);
                                            analysisResult.replace(dir, list);
                                        }
                                    }
                                }
                            }

                            completeCount[0]++;
                            if(completeCount[0] == copyList.size()) {
                                HandlerCompat.createAsync(Looper.getMainLooper()).post(onComplete);
                            }
                        });
                } catch (IOException e) {
                    e.printStackTrace();
                    completeCount[0]++;
                    if(completeCount[0] == copyList.size()) {
                        HandlerCompat.createAsync(Looper.getMainLooper()).post(onComplete);
                    }
                }
            }
        });
    }

    public List<Uri> getAnalysisResult(FaceDirection direction) {
        return analysisResult.get(direction);
    }
}
