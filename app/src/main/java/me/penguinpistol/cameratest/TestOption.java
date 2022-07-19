package me.penguinpistol.cameratest;

import android.util.Log;

public final class TestOption {
    public static boolean isCameraUp;
    public static boolean isSquareCamera;
    public static float angleXZ;
    public static float angleY;
    public static float position;

    public static void print() {
        Log.d("TestOption",
                    "TestOption >> \n" +
                    "isCameraUp: " + isCameraUp + "\n" +
                    "isSquareCamera: " + isSquareCamera + "\n" +
                    "angleXZ: " + angleXZ + "\n" +
                    "angleY: " + angleY + "\n" +
                    "position: " + position
                );
    }
}
