package me.penguinpistol.cameratest.v1.camera;


public interface FaceDetectionListener {
    void onDetected(FaceDirection direction);
    void onFailure(DetectionFailure reason);
}
