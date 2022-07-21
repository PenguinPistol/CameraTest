package me.penguinpistol.cameratest;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import me.penguinpistol.cameratest.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mBinding.btnStartV1.setOnClickListener(v -> start(TakePictureV1Activity.class));
        mBinding.btnStartV2.setOnClickListener(v -> start(TakePictureV2Activity.class));
        mBinding.btnOption.setOnClickListener(v -> start(OptionActivity.class));
    }

    private void start(Class<?> target) {
        startActivity(new Intent(this, target));
    }
}