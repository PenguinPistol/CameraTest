package me.penguinpistol.cameratest.v1.camera;

import me.penguinpistol.cameratest.R;

public enum DetectionFailure {
    NO_FACE("얼굴이 감지되지 않았습니다", -1)
    , PITCH_CCW("고개를 내려주세요", R.drawable.shape_pitch_ccw)
    , PITCH_CW("고개를 올려주세요", R.drawable.shape_pitch_cw)
    , ROLL_CCW("머리를 왼쪽으로 세워주세요", R.drawable.shape_roll_ccw)
    , ROLL_CW("머리를 오른쪽으로 세워주세요", R.drawable.shape_roll_cw)
    , YAW_CCW("고개를 오른쪽으로 돌려주세요", R.drawable.shape_yaw_ccw)
    , YAW_CW("고개를 왼쪽으로 돌려주세요", R.drawable.shape_yaw_cw)
    , MORE_ZOOM_IN("좀 더 가까이 와주세요", R.drawable.shape_zoom_in)
    , MORE_ZOOM_OUT("좀 더 멀리 가주세요", R.drawable.shape_zoom_out)
    , MOVE_LEFT("머리를 왼쪽으로 이동해주세요", R.drawable.ic_move_left)
    , MOVE_RIGHT("머리를 오른쪽으로 이동해주세요", R.drawable.ic_move_right)
    , MOVE_UP("머리를 위쪽으로 이동해주세요", R.drawable.ic_move_up)
    , MOVE_DOWN("머리를 아래쪽으로 이동해주세요", R.drawable.ic_move_down)
    ;

    final String message;
    final int imageRes;

    DetectionFailure(String message, int imageRes) {
        this.message = message;
        this.imageRes = imageRes;
    }

    public String getMessage() {
        return message;
    }

    public int getImageRes() {
        return imageRes;
    }
}
