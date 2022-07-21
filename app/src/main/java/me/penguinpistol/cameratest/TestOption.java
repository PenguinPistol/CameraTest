package me.penguinpistol.cameratest;

import android.util.Log;

public final class TestOption {
    public static float cameraPos = 1.0f;
    public static int   takeInterval = 250;
    public static boolean usePrepare = false;
    public static float angleXZ = 10.0f;
    public static float angleY = 5.0f;
    public static float position = 25.0f;
    public static float eyesOpen = 0.65f;
    public static float widthRatio = 0.12f;
    public static float targetWidthRatio = 0.52f;

    public static String getInterval() {
        return String.valueOf((takeInterval * 0.001f));
    }

    public static void print() {
        Log.d("TestOption",
                    "TestOption >> \n" +
                    "cameraPos: " + cameraPos + "\n" +
                    "takeInterval: " + takeInterval + "\n" +
                    "usePrepare: " + usePrepare + "\n" +
                    "angleXZ: " + angleXZ + "\n" +
                    "angleY: " + angleY + "\n" +
                    "position: " + position + "\n" +
                    "eyesOpen: " + eyesOpen + "\n" +
                    "widthRatio: " + widthRatio
                );
    }
}
