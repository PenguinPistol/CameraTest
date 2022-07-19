package me.penguinpistol.cameratest.camera;

public enum DetectionErrorValue {
    WIDTH_RATIO(0.12f),                 // 가로비율
    POSITION(12f),                       // 중심점
    ANGLE_XZ(10f),                       // xz 각도
    ANGLE_Y(5f),                        // y 각도
    EYE_OPEN(0.65f),                    // 눈 열림 최소
    ;

    final float value;

    DetectionErrorValue(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }
}
