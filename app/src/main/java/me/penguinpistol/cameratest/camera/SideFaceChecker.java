package me.penguinpistol.cameratest.camera;

import android.util.Log;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;

public class SideFaceChecker {
    private FaceDirection mDirection;

    public void setFaceDirection(FaceDirection direction) {
        mDirection = direction;
    }

    // 자동촬영 판정
    public boolean check(Face face) {
        FaceContour contour = face.getContour(FaceContour.FACE);

        if (contour == null) {
            return false;
        }

        // 정면카메라의 경우 좌우반전
        float leftEyeOpen = face.getRightEyeOpenProbability() == null ? 0 : face.getRightEyeOpenProbability();
        float rightEyeOpen = face.getLeftEyeOpenProbability() == null ? 0 : face.getLeftEyeOpenProbability();

        boolean angle = checkAngle(face.getHeadEulerAngleX(), face.getHeadEulerAngleY(), face.getHeadEulerAngleZ());
        boolean eyes = checkEyesOpen(leftEyeOpen, rightEyeOpen);

        if(!angle) {
            Log.d("CameraXHelper", "Angle Failure");
        }
        if(!eyes) {
            Log.d("CameraXHelper", "Eyes Failure");
        }

        return checkAngle(face.getHeadEulerAngleX(), face.getHeadEulerAngleY(), face.getHeadEulerAngleZ())
                && checkEyesOpen(leftEyeOpen, rightEyeOpen)
                ;
    }

    /**
     * 얼굴각도 판정<br/>
     * x : pitch(상하각도)<br/>
     * y : yaw(좌우각도)<br/>
     * z : roll(회전)
     */
    private boolean checkAngle(float x, float y, float z) {
        if (mDirection == null) {
            return false;
        }

        Log.d("CameraXHelper", String.format("angle[%f, %f, %f]", x, y, z));

        boolean checkDownX = (-DetectionErrorValue.ANGLE_XZ.getValue() < x);
        boolean checkUpX = (x < DetectionErrorValue.ANGLE_XZ.getValue());
        boolean checkZ = (-DetectionErrorValue.ANGLE_XZ.getValue() < z) && (z < DetectionErrorValue.ANGLE_XZ.getValue());
        boolean checkMinY = mDirection.checkMin(y, DetectionErrorValue.ANGLE_Y.getValue());
        boolean checkMaxY = mDirection.checkMax(y, DetectionErrorValue.ANGLE_Y.getValue());

        return checkDownX && checkUpX && checkZ && checkMinY && checkMaxY;
    }

    /**
     * 눈 열림 판정<br/>
     * 눈 뜸 : ~ 1.0<br/>
     * 눈 감음 : ~ 0.0<br/>
     */
    private boolean checkEyesOpen(float left, float right) {
        Log.d("CameraXHelper", String.format("eyes [%f, %f]", left, right));

        return left > DetectionErrorValue.EYE_OPEN.getValue() && right > DetectionErrorValue.EYE_OPEN.getValue();
    }
}
