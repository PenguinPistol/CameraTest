package me.penguinpistol.cameratest.camera;


public interface FaceDetectionListener {
    void onDetected(FaceDirection direction);
    void onFailure(DetectionFailure reason);
}
