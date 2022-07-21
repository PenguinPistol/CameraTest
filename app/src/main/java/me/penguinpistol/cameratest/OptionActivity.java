package me.penguinpistol.cameratest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import me.penguinpistol.cameratest.databinding.ActivityOptionBinding;

public class OptionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityOptionBinding binding = ActivityOptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 카메라위치
        binding.sliderCameraPos.setValue(TestOption.cameraPos);
        binding.sliderCameraPos.addOnChangeListener((slider, value, fromUser) -> {
            TestOption.cameraPos = value;
            binding.invalidateAll();
        });

        // 자동촬영 간격
        binding.sliderTakeInterval.setValue((TestOption.takeInterval * 0.001f));
        binding.sliderTakeInterval.addOnChangeListener((slider, value, fromUser) -> {
            TestOption.takeInterval = (int)(value * 1000);
            binding.invalidateAll();
        });

        // 자동촬영 준비 활성화
        binding.switchUsePrepare.setChecked(TestOption.usePrepare);
        binding.switchUsePrepare.setOnCheckedChangeListener((v, b) -> TestOption.usePrepare = b);

        // 점선영역크기
        binding.sliderTargetWidth.setValue(TestOption.targetWidthRatio);
        binding.sliderTargetWidth.addOnChangeListener((slider, value, fromUser) -> {
            TestOption.targetWidthRatio = value;
            binding.invalidateAll();
        });

        // 머리각도
        binding.sliderAngleXz.setValue(TestOption.angleXZ);
        binding.sliderAngleXz.addOnChangeListener((slider, value, fromUser) -> {
            TestOption.angleXZ = value;
            binding.invalidateAll();
        });

        // 좌우각도
        binding.sliderAngleY.setValue(TestOption.angleY);
        binding.sliderAngleY.addOnChangeListener((slider, value, fromUser) -> {
            TestOption.angleY = value;
            binding.invalidateAll();
        });

        // 위치
        binding.sliderPosition.setValue(TestOption.position);
        binding.sliderPosition.addOnChangeListener((slider, value, fromUser) -> {
            TestOption.position = value;
            binding.invalidateAll();
        });

        // 눈열림
        binding.sliderEyes.setValue(TestOption.eyesOpen);
        binding.sliderEyes.addOnChangeListener(((slider, value, fromUser) -> {
            TestOption.eyesOpen = value;
            binding.invalidateAll();
        }));

        binding.btnConfirm.setOnClickListener(v -> finish());
    }
}