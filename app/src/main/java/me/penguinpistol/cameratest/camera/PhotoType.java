package me.penguinpistol.cameratest.camera;

import java.util.Arrays;
import java.util.List;

public enum PhotoType {
    FRONT("정면", "")
    , LEFT("왼쪽", "오른쪽")
    , RIGHT("오른쪽", "왼쪽");

    final String label;
    final String mirror;

    PhotoType(String label, String mirror) {
        this.label = label;
        this.mirror = mirror;
    }

    public String getLabel() {
        return label;
    }

    public String getMirror() {
        return mirror;
    }
}
