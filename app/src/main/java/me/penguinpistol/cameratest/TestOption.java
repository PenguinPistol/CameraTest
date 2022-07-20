package me.penguinpistol.cameratest;

import android.util.Log;

public final class TestOption {
    public static boolean isCameraUp = false;
    public static boolean isSquareCamera = false;
    public static float angleXZ = 10.0f;
    public static float angleY = 5.0f;
    public static float position = 15.0f;
    public static float eyesOpen = 0.65f;
    public static float widthRatio = 0.12f;

    public static boolean isIsCameraUp() {
        return isCameraUp;
    }

    public static void setIsCameraUp(boolean isCameraUp) {
        TestOption.isCameraUp = isCameraUp;
    }

    public static boolean isIsSquareCamera() {
        return isSquareCamera;
    }

    public static void setIsSquareCamera(boolean isSquareCamera) {
        TestOption.isSquareCamera = isSquareCamera;
    }

    public static float getAngleXZ() {
        return angleXZ;
    }

    public static void setAngleXZ(float angleXZ) {
        TestOption.angleXZ = angleXZ;
    }

    public static float getAngleY() {
        return angleY;
    }

    public static void setAngleY(float angleY) {
        TestOption.angleY = angleY;
    }

    public static float getPosition() {
        return position;
    }

    public static void setPosition(float position) {
        TestOption.position = position;
    }

    public static float getEyesOpen() {
        return eyesOpen;
    }

    public static void setEyesOpen(float eyesOpen) {
        TestOption.eyesOpen = eyesOpen;
    }

    public static float getWidthRatio() {
        return widthRatio;
    }

    public static void setWidthRatio(float widthRatio) {
        TestOption.widthRatio = widthRatio;
    }

    public static void print() {
        Log.d("TestOption",
                    "TestOption >> \n" +
                    "isCameraUp: " + isCameraUp + "\n" +
                    "isSquareCamera: " + isSquareCamera + "\n" +
                    "angleXZ: " + angleXZ + "\n" +
                    "angleY: " + angleY + "\n" +
                    "position: " + position + "\n" +
                    "eyesOpen: " + eyesOpen + "\n" +
                    "widthRatio: " + widthRatio
                );
    }
}
