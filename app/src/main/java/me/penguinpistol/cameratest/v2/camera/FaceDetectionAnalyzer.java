package me.penguinpistol.cameratest.v2.camera;

import android.graphics.RectF;
import android.media.Image;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import me.penguinpistol.cameratest.TestOption;

public class FaceDetectionAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = "FaceDetectionAnalyzer";

    /**
     * 평균 남자 얼굴면적 15.7 x 23.6 cm
     * 평균 여자 얼굴면적 14.7 x 22.3 cm
     * 의 세로비율
    */
    private static final float TARGET_HEIGHT_RATIO = 1.4F;
//    private static final float TARGET_HEIGHT_RATIO = 1.517F;

    private final FaceDetector mDetector;
    private final GraphicOverlay mGraphic;
    private final Runnable mOnDetected;

    private FrontChecker mFaceChecker;

    public FaceDetectionAnalyzer(GraphicOverlay graphic, Runnable onDetected) {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .build();

        mDetector = FaceDetection.getClient(options);
        mGraphic = graphic;
        mOnDetected = onDetected;
    }

    public void setImageSize(Size frameSize) {
        Size imageSize = new Size(
                Math.min(frameSize.getWidth(), frameSize.getHeight()),
                Math.max(frameSize.getWidth(), frameSize.getHeight()));

        float targetWidth = imageSize.getWidth() * TestOption.targetWidthRatio;
        RectF targetRect = new RectF(
                0,
                0,
                targetWidth,
                targetWidth * TARGET_HEIGHT_RATIO
        );
        targetRect.offset(
                (imageSize.getWidth()  - targetRect.width()) * 0.5f,
                (imageSize.getHeight()  - targetRect.height()) * 0.5f
        );

        mGraphic.init(imageSize, targetRect);
        mFaceChecker = new FrontChecker(targetRect);
    }

    @OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        if(mFaceChecker == null) {
            return;
        }

        Image mediaImage = imageProxy.getImage();
        if(mediaImage != null) {
            int rotate = imageProxy.getImageInfo().getRotationDegrees();
            InputImage inputImage = InputImage.fromMediaImage(mediaImage, rotate);

            mDetector.process(inputImage)
                    .addOnSuccessListener(faces -> {
                        if (faces.size() > 0) {
                            Face face = faces.get(0);
                            if (mFaceChecker.check(face)) {
                                mOnDetected.run();
                            } else {
                                mGraphic.setFailure(mFaceChecker.getFailureReason());
                            }

//                            FaceContour contour = face.getContour(FaceContour.FACE);
//                            if(contour != null) {
//                                mGraphic.setFaceContour(contour);
//                            }
                        }
                    })
                    .addOnFailureListener(Throwable::printStackTrace)
                    .addOnCompleteListener(task -> imageProxy.close());
        } else {
            Log.e(TAG, "===============================================================");
            Log.e(TAG, "analyze >> mediaImage is NULL");
            Log.e(TAG, "===============================================================");
        }
    }
}
