package me.penguinpistol.cameratest;

import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import me.penguinpistol.cameratest.camera.CameraXHelper;
import me.penguinpistol.cameratest.camera.PhotoType;
import me.penguinpistol.cameratest.databinding.FragmentTakePictureBinding;

public class TakePictureFragment extends Fragment {
    private FragmentTakePictureBinding mBinding;
    private TakePictureViewModel mViewModel;
    private CameraXHelper mCameraHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentTakePictureBinding.inflate(inflater, container, false);
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

        return mBinding.getRoot();
    }

}
