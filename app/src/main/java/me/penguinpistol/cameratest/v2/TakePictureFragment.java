package me.penguinpistol.cameratest.v2;

import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.os.HandlerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import me.penguinpistol.cameratest.R;
import me.penguinpistol.cameratest.TestOption;
import me.penguinpistol.cameratest.common.AlertDialog;
import me.penguinpistol.cameratest.databinding.FragmentTakePictureV2Binding;
import me.penguinpistol.cameratest.v2.camera.CameraXHelper;

public class TakePictureFragment extends Fragment {
    private static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;
    private static final int PREPARE_TIME = 3000;
    private static final int PROGRESS_TICK = 1;

    private FragmentTakePictureV2Binding mBinding;
    private TakePictureViewModel mViewModel;
    private CameraXHelper mCameraHelper;
    private ScheduledExecutorService mPrepareTimer;
    private int mCurrentTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentTakePictureV2Binding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(requireActivity()).get(TakePictureViewModel.class);

        if(TestOption.isCameraUp) {
            changeGuidePos();
        }

        mCameraHelper = new CameraXHelper(requireActivity(), mBinding.preview);
        mCameraHelper.startCamera(() -> {
            AlertDialog dialog = new AlertDialog(requireActivity(), "자동촬영을 시작합니다.\n고개를 좌우로 천천히 움직여주세요.");
            dialog.setOnDismissListener(dialogInterface -> {
                startProcess();
            });
            dialog.show();
        });

        mBinding.progressPrepare.setMax(PREPARE_TIME);

        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 저장된 이미지 전체 삭제
        for (Uri uri : mViewModel.getProcessResult()) {
            mViewModel.removeFile(uri);
        }
    }

    @Override
    public void onDestroy() {
        if(mPrepareTimer != null && !mPrepareTimer.isShutdown()) {
            mPrepareTimer.shutdown();
            mPrepareTimer = null;
        }

        if(mCameraHelper != null) {
            mCameraHelper.shutdownProcess();
        }

        super.onDestroy();
    }

    private void startProcess() {
        mBinding.progressPrepare.setProgress(PREPARE_TIME);
        mBinding.tvProgressTime.setText(String.valueOf(PREPARE_TIME));

        mBinding.groupProgress.setVisibility(View.VISIBLE);

        mCurrentTime = 0;
        mPrepareTimer = Executors.newSingleThreadScheduledExecutor();
        mPrepareTimer.scheduleAtFixedRate(() -> {
            mCurrentTime += PROGRESS_TICK;
            setProgress();

            if(mCurrentTime == PREPARE_TIME) {
                HandlerCompat.createAsync(Looper.getMainLooper()).post(() -> {
                    mBinding.groupProgress.setVisibility(View.GONE);
                    mBinding.groupProcessing.setVisibility(View.VISIBLE);
                });
                mCameraHelper.startProcess(this::onComplete);
                mPrepareTimer.shutdown();
                mPrepareTimer = null;
            }
        }, 0, PROGRESS_TICK, TIME_UNIT);
    }

    private void onComplete(List<Uri> result) {
        mViewModel.setProcessResult(result);

        HandlerCompat.createAsync(Looper.getMainLooper()).post(() -> {
            mBinding.groupProcessing.setVisibility(View.GONE);

            mViewModel.getNavController().navigate(R.id.action_analysis);
        });
    }

    private void setProgress() {
        HandlerCompat.createAsync(Looper.getMainLooper()).post(() -> {
            int value = PREPARE_TIME - mCurrentTime;
            mBinding.tvProgressTime.setText(String.valueOf(TIME_UNIT.toSeconds(value) + 1));
            mBinding.progressPrepare.setProgress(value);
        });
    }

    private void changeGuidePos() {
        float ratioBottom = 0.85F;
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(mBinding.getRoot());
        constraintSet.setGuidelinePercent(R.id.guideline_b, ratioBottom);
        constraintSet.setGuidelinePercent(R.id.guideline_t,  ratioBottom - 1f);
        constraintSet.applyTo(mBinding.getRoot());
    }
}
