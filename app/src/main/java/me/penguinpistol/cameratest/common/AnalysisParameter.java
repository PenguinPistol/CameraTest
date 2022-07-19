package me.penguinpistol.cameratest.common;

import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

import me.penguinpistol.cameratest.camera.FaceDirection;

public class AnalysisParameter {
    private final Map<FaceDirection, Uri> images;
    private float pxToCm;

    public AnalysisParameter() {
        images = new HashMap<>();
        pxToCm = 0;
    }

    public float getPxToCm() {
        return pxToCm;
    }

    public void setPxToCm(float pxToCm) {
        this.pxToCm = pxToCm;
    }

    public void setImage(FaceDirection direction, Uri uri) {
        if(images.containsKey(direction)) {
            images.replace(direction, uri);
        } else {
            images.put(direction, uri);
        }
    }

    public Uri getImageUri(FaceDirection direction) {
        return images.get(direction);
    }
}
