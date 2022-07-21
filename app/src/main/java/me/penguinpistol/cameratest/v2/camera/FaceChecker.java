package me.penguinpistol.cameratest.v2.camera;

import android.util.Log;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;

import me.penguinpistol.cameratest.TestOption;

public class FaceChecker {

    /**
     * 기준 각도별 적합한 얼굴인지 판정하여 해당하는 각도를 반환한다.<br/>
     * (위치판정 -> 얼굴크기 판정)<br/>
     * -> pitch/roll 각도판정<br/>
     * -> 눈열림/입다뭄 판정<br/>
     * -> yaw 각도판정
     */
    public FaceDirection checkDirection(Face face) {
        FaceContour contour = face.getContour(FaceContour.FACE);

        if(contour == null) {
            Log.d("TakePicture", "face contour not found!!");
            return null;
        }

        // 눈 없는경우 열림으로 처리
        float leftEyeOpen = face.getLeftEyeOpenProbability() == null ? 1 : face.getLeftEyeOpenProbability();
        float rightEyeOpen = face.getRightEyeOpenProbability() == null ? 1 : face.getRightEyeOpenProbability();

        boolean headAngle = checkAngle(face.getHeadEulerAngleX(), face.getHeadEulerAngleZ());
        boolean eyesOpen = checkEyesOpen(leftEyeOpen, rightEyeOpen);

        if(headAngle && eyesOpen) {
            float headY = face.getHeadEulerAngleY();
            for(FaceDirection dir : FaceDirection.values()) {
                boolean checkMin = dir.checkMin(headY, TestOption.angleY);
                boolean checkMax = dir.checkMax(headY, TestOption.angleY);
                if(checkMin && checkMax) {
                    return dir;
                }
            }
        }

        return null;
    }


    /**
     * 얼굴각도 판정<br/>
     * x : pitch(상하각도)<br/>
     * z : roll(회전)
     */
    private boolean checkAngle(float x, float z) {
        boolean checkX = (-TestOption.angleXZ < x) && (x < TestOption.angleXZ);
        boolean checkZ = (-TestOption.angleXZ < z) && (z < TestOption.angleXZ);
        return checkX && checkZ;
    }

    /**
     * 눈 열림 판정<br/>
     * 눈 뜸 : ~ 1.0<br/>
     * 눈 감음 : ~ 0.0<br/>
     */
    private boolean checkEyesOpen(float left, float right) {
        return (left > TestOption.eyesOpen) && (right > TestOption.eyesOpen);
    }
}
