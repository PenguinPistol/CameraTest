package me.penguinpistol.cameratest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Map;

import me.penguinpistol.cameratest.camera.FaceDirection;
import me.penguinpistol.cameratest.databinding.ActivityLabTakePictureBinding;

public class LabTakePictureActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSION = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private ActivityLabTakePictureBinding mBinding;
    private TakePictureViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityLabTakePictureBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mViewModel = new ViewModelProvider(this).get(TakePictureViewModel.class);

        NavHostFragment navHostFragment = (NavHostFragment)getSupportFragmentManager().findFragmentById(R.id.nav_host);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            mViewModel.setNavController(navController);
        }

        if(allPermissionGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_PERMISSION) {
            if(allPermissionGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "모든 권한을 허용해주세요", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        // 저장된 사진 전체 삭제
        Map<FaceDirection, Uri> photoMap = mViewModel.getTakePhotoMap();
        for(FaceDirection direction : photoMap.keySet()) {
            mViewModel.removeFile(photoMap.get(direction));
        }
        super.onDestroy();
    }

    private boolean allPermissionGranted() {
        for(String permission : REQUIRED_PERMISSIONS) {
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void startCamera() {
        if(mViewModel.getNavController() != null) {
            // alert 띄우기
        }
    }
}