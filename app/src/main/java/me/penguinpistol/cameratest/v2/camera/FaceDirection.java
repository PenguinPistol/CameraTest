package me.penguinpistol.cameratest.v2.camera;

public enum FaceDirection {
    FRONT(0, "정면"),
    LEFT_30(30, "좌30º"),
    LEFT_45(45, "좌45º"),
    RIGHT_30(-30, "우30º"),
    RIGHT_45(-45, "우45º"),
    ;

    final float angle;
    final String label;

    FaceDirection(float angle, String label) {
        this.angle = angle;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean checkMin(float angleY, float errorValue) {
        return (angle - errorValue) < angleY;
    }

    public boolean checkMax(float angleY, float errorValue) {
        return angleY < (angle + errorValue);
    }
}
