package me.penguinpistol.cameratest.v1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import me.penguinpistol.cameratest.R;
import me.penguinpistol.cameratest.TestOption;
import me.penguinpistol.cameratest.v1.camera.CameraXHelper;
import me.penguinpistol.cameratest.databinding.FragmentTakePictureV1Binding;

public class TakePictureFragment extends Fragment {
    private FragmentTakePictureV1Binding mBinding;
    private TakePictureViewModel mViewModel;
    private CameraXHelper mCameraHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentTakePictureV1Binding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(requireActivity()).get(TakePictureViewModel.class);

        mCameraHelper = new CameraXHelper(requireActivity(), mBinding.getRoot(), () -> {
            mCameraHelper.startAnalysis(() -> mCameraHelper.takePicture());
        });
        mCameraHelper.setOnTakePictureCallback(uri -> {
            Bundle bundle = new Bundle(1);
            bundle.putParcelable("uri", uri);
            mViewModel.getNavController().navigate(R.id.action_result_front, bundle);
        });
        mCameraHelper.startCamera();

        changeGuidePos();

        return mBinding.getRoot();
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
