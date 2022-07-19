package me.penguinpistol.cameratest.v2;

import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;

public class TakePictureViewModel extends ViewModel {

    private NavController navController;

    public NavController getNavController() {
        return navController;
    }

    public void setNavController(NavController navController) {
        this.navController = navController;
    }
}
