package me.penguinpistol.cameratest.v1;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import me.penguinpistol.cameratest.R;
import me.penguinpistol.cameratest.TestOption;
import me.penguinpistol.cameratest.v1.camera.CameraXHelper;
import me.penguinpistol.cameratest.v1.camera.PhotoType;
import me.penguinpistol.cameratest.common.AlertDialog;
import me.penguinpistol.cameratest.databinding.FragmentTakePictureV1SideBinding;

public class TakePictureSideFragment extends Fragment {

    private FragmentTakePictureV1SideBinding mBinding;
    private TakePictureViewModel mViewModel;
    private CameraXHelper mCameraHelper;
    private PhotoType mPhotoType;
    private List<Uri> mTakePhotos;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if(args != null) {
            mPhotoType = (PhotoType)args.getSerializable(TakePictureViewModel.KEY_SIDE_TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentTakePictureV1SideBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(requireActivity()).get(TakePictureViewModel.class);

        if(mPhotoType == null || mPhotoType == PhotoType.FRONT) {
            Log.w("CameraXHelper", "PhotoType is NULL");
            mViewModel.getNavController().navigateUp();
            return null;
        }

        mViewModel.clearTakePhoto(mPhotoType);

        mCameraHelper = new CameraXHelper(requireActivity(), mBinding.getRoot(), () -> {
            AlertDialog dialog = new AlertDialog(requireActivity(), mPhotoType.getLabel() + "얼굴 촬영을 시작합니다.\n얼굴을 정면을 맞춰주세요.");
            dialog.setOnDismissListener(dialogInterface -> {
                // start front analyzer
                // onDetection -> startProcess()
                mCameraHelper.startAnalysis(this::onDetected);
            });
            dialog.show();
        });
        mCameraHelper.setOnTakePictureCallback(this::onTakePicture);
        mCameraHelper.startCamera();

        if(TestOption.isCameraUp) {
            changeGuidePos();
        }

        return mBinding.getRoot();
    }

    private void onDetected() {
        String message = String.format("%s얼굴을 5초간 연속 촬영합니다.\n머리를 %s 방향으로 천천히 돌려주세요.", mPhotoType.getLabel(), mPhotoType.getMirror());
        AlertDialog dialog = new AlertDialog(requireActivity(), message);
        dialog.setOnDismissListener(dialogInterface -> {
            mTakePhotos = new ArrayList<>();
            mCameraHelper.startProcess(this::onComplete);
        });
        dialog.show();
        mBinding.graphicOverlay.setVisibility(View.GONE);
    }

    private void onTakePicture(Uri uri) {
        mTakePhotos.add(uri);
    }

    private void onComplete() {
        AlertDialog dialog = new AlertDialog(requireActivity(), "촬영이 종료되었습니다.");
        dialog.setOnDismissListener(dialogInterface -> {
            mViewModel.setTakePhoto(mPhotoType, mTakePhotos);

            Bundle args = new Bundle(1);
            args.putSerializable(TakePictureViewModel.KEY_SIDE_TYPE, mPhotoType);
            mViewModel.getNavController().navigate(R.id.action_start_progress, args);
        });
        dialog.show();
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
