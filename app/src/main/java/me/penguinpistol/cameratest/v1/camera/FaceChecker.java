package me.penguinpistol.cameratest.v1.camera;

import android.graphics.PointF;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.google.android.material.math.MathUtils;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;

import java.util.List;
import java.util.Locale;

import me.penguinpistol.cameratest.TestOption;

public class FaceChecker {
    private final RectF mTargetRect;

    private FaceDirection mDirection;
    private DetectionFailure failureReason;

    private boolean isDebug = false;
    private String debugText;

    public FaceChecker(@NonNull RectF targetRect) {
        mTargetRect = targetRect;
    }

    public void setDirection(FaceDirection dir) {
        mDirection = dir;
    }

    public FaceDirection getDirection() {
        return mDirection;
    }

    public DetectionFailure getFailureReason() {
        return failureReason;
    }

    // 자동촬영 판정
    public boolean check(Face face) {
        FaceContour contour = face.getContour(FaceContour.FACE);

        if(contour == null) {
            return false;
        }

        RectF faceRect = getFaceRect(contour.getPoints());

        // 정면카메라의 경우 좌우반전
        float leftEyeOpen = face.getRightEyeOpenProbability() == null ? 0 : face.getRightEyeOpenProbability();
        float rightEyeOpen = face.getLeftEyeOpenProbability() == null ? 0 : face.getLeftEyeOpenProbability();

        if(isDebug) {
            debugText = "current dir: " + mDirection.name() + "\n";

            // debugText 출력을 위해 각각 따로 호출
            boolean checkAngle = checkAngle(face.getHeadEulerAngleX(), face.getHeadEulerAngleY(), face.getHeadEulerAngleZ());
            boolean checkPosition = checkPosition(faceRect);
            boolean checkWidthRatio = checkWidthRatio(faceRect.width());
            boolean checkEyesOpen = checkEyesOpen(leftEyeOpen, rightEyeOpen);

            return checkAngle && checkPosition && checkWidthRatio && checkEyesOpen;
        }

        return checkAngle(face.getHeadEulerAngleX(), face.getHeadEulerAngleY(), face.getHeadEulerAngleZ())
                && checkPosition(faceRect)
                && checkWidthRatio(faceRect.width())
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
        if(mDirection == null) {
            return false;
        }

        boolean checkDownX = (-TestOption.angleXZ < x);
        boolean checkUpX = (x < TestOption.angleXZ);
        boolean checkMinZ = -TestOption.angleXZ < z;
        boolean checkMaxZ = z < TestOption.angleXZ;
        boolean checkMinY = mDirection.checkMin(y, TestOption.angleY);
        boolean checkMaxY = mDirection.checkMax(y, TestOption.angleY);

        if(!checkDownX) {
            failureReason = DetectionFailure.PITCH_CW;
        } else if(!checkUpX) {
            failureReason = DetectionFailure.PITCH_CCW;
        } else if(!checkMinZ) {
            failureReason = DetectionFailure.ROLL_CW;
        } else if(!checkMaxZ) {
            failureReason = DetectionFailure.ROLL_CCW;
        } else if(!checkMinY) {
            failureReason = DetectionFailure.YAW_CW;
        } else if(!checkMaxY) {
            failureReason = DetectionFailure.YAW_CCW;
        }

        if(isDebug) {
            debugText += String.format(Locale.getDefault(), "Angle[%f, %f, %f]\n", x, y, z);
            debugText += String.format(
                    Locale.getDefault(),
                    "Angle Check[x: %b / z: %b / minY: %b / maxY: %b]\n",
                    (checkDownX && checkUpX),
                    (checkMinZ && checkMaxZ),
                    checkMinY,
                    checkMaxY);
        }

        return checkDownX && checkUpX && checkMinZ && checkMaxZ && checkMinY && checkMaxY;
    }

    /**
     * 위치판정<br/>
     * FaceRect의 중점과 TargetRect의 중점간의 거리를 기준으로 얼굴이 적절한 위치에 있는지 확인한다.
     */
    private boolean checkPosition(RectF face) {
        float distance = MathUtils.dist(face.centerX(), face.centerY(), mTargetRect.centerX(), mTargetRect.centerY());
        if(isDebug) {
            debugText += String.format(Locale.getDefault(), "Distance: %f\n", distance);
            debugText += String.format(Locale.getDefault(), "Target center[%f, %f]\nFace center[%f, %f]\n", mTargetRect.centerX(), mTargetRect.centerY(), face.centerX(), face.centerY());
        }
        boolean check = distance < TestOption.position;

        if(!check) {
            if(face.centerX() - mTargetRect.centerX() < -TestOption.position) {
                failureReason = DetectionFailure.MOVE_LEFT;
            } else if(face.centerX() - mTargetRect.centerX() > TestOption.position) {
                failureReason = DetectionFailure.MOVE_RIGHT;
            } else if(face.centerY() - mTargetRect.centerY() < -TestOption.position) {
                failureReason = DetectionFailure.MOVE_DOWN;
            } else if(face.centerY() - mTargetRect.centerY() > TestOption.position) {
                failureReason = DetectionFailure.MOVE_UP;
            }
        }

        return check;
    }

    /**
     * 가로비율 판정<br/>
     * FaceRect의 가로길이와 TargetRect의 가로길이의 비율을 비교하여 얼굴이 TargetRect 안에 들어왔는지 판정한다.
     */
    private boolean checkWidthRatio(float faceWidth) {
        float ratio = faceWidth / mTargetRect.width();
        if(isDebug) {
            debugText += String.format(Locale.getDefault(), "Face width: %f\nTarget width: %f\nFace ratio: %f\n", faceWidth, mTargetRect.width(), ratio);
        }

        boolean minCheck = (1-TestOption.widthRatio) < ratio;
        boolean maxCheck = ratio < (1+TestOption.widthRatio);

        if(!minCheck) {
            failureReason = DetectionFailure.MORE_ZOOM_IN;
        } else if(!maxCheck) {
            failureReason = DetectionFailure.MORE_ZOOM_OUT;
        }

        return minCheck && maxCheck;
    }

    /**
     * 눈 열림 판정<br/>
     * 눈 뜸 : ~ 1.0<br/>
     * 눈 감음 : ~ 0.0<br/>
     */
    private boolean checkEyesOpen(float left, float right) {
        if(isDebug) {
            debugText += String.format(Locale.getDefault(), "eyes open[%f, %f]\n", left, right);
        }
        return left > TestOption.eyesOpen && right > TestOption.eyesOpen;
    }

    // 얼굴 Landmark -> Rect 변환
    private RectF getFaceRect(List<PointF> landmarks) {
        return new RectF(
                (float)landmarks.stream().mapToDouble(x -> x.x).min().orElse(0),
                (float)landmarks.stream().mapToDouble(x -> x.y).min().orElse(0),
                (float)landmarks.stream().mapToDouble(x -> x.x).max().orElse(0),
                (float)landmarks.stream().mapToDouble(x -> x.y).max().orElse(0)
        );
    }

    public void setDebug(boolean debug) {
        isDebug = debug;
    }

    public String getDebugText() {
        return debugText;
    }
}
