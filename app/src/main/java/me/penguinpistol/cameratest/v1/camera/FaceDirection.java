package me.penguinpistol.cameratest.v1.camera;

public enum FaceDirection {
    FRONT(0),
    LEFT_30(-30),
    LEFT_45(-45),
    RIGHT_30(30),
    RIGHT_45(45),
    ;

    final float angle;

    FaceDirection(float angle) {
        this.angle = angle;
    }

    public boolean checkMin(float angleY, float errorValue) {
        return (angle - errorValue) < angleY;
    }

    public boolean checkMax(float angleY, float errorValue) {
        return angleY < (angle + errorValue);
    }
}
