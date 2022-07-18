package me.penguinpistol.cameratest.camera;

import java.util.Arrays;
import java.util.List;

public enum PhotoType {
    FRONT(Arrays.asList(FaceDirection.FRONT) )
    , LEFT(Arrays.asList(FaceDirection.LEFT_30, FaceDirection.LEFT_45))
    , RIGHT(Arrays.asList(FaceDirection.RIGHT_30, FaceDirection.RIGHT_45));

    final List<FaceDirection> order;

    PhotoType(List<FaceDirection> order) {
        this.order = order;
    }

    public List<FaceDirection> getOrder() {
        return order;
    }
}
