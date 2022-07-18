package me.penguinpistol.cameratest.camera;

public enum DetectionFailure {
    NO_FACE("얼굴 없음")
    , ANGLE_X_DOWN("고개를 내려주세요")
    , ANGLE_X_UP("고개를 올려주세요")
    , ANGLE_Z("머리를 똑바로 세워주세요")
    , MORE_ZOOM_IN("좀 더 가까이 와주세요")
    , MORE_ZOOM_OUT("좀 더 멀리 가주세요")
    , TURN_LEFT("고개를 왼쪽으로 돌려주세요")
    , TURN_RIGHT("고개를 오른쪽으로 돌려주세요")
    , MOVE_LEFT("머리를 왼쪽으로 이동해주세요")
    , MOVE_RIGHT("머리를 오른쪽으로 이동해주세요")
    , MOVE_UP("머리를 위쪽으로 이동해주세요")
    , MOVE_DOWN("머리를 아래쪽으로 이동해주세요")
    ;

    final String message;

    DetectionFailure(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
