package me.penguinpistol.cameratest;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

import me.penguinpistol.cameratest.camera.CameraXHelper;
import me.penguinpistol.cameratest.camera.PhotoType;
import me.penguinpistol.cameratest.databinding.FragmentTakePictureSideBinding;

public class TakePictureSideFragment extends Fragment {

    private FragmentTakePictureSideBinding mBinding;
    private TakePictureViewModel mViewModel;
    private CameraXHelper mCameraHelper;
    private PhotoType mPhotoType;

    private ArrayList<Uri> mTakePhotoList;

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
        mBinding = FragmentTakePictureSideBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(requireActivity()).get(TakePictureViewModel.class);

        if(mPhotoType == null || mPhotoType == PhotoType.FRONT) {
            Log.w("CameraXHelper", "PhotoType is NULL");
            mViewModel.getNavController().navigateUp();
            return null;
        }

        mCameraHelper = new CameraXHelper(requireActivity(), mBinding.getRoot(), () -> {
            AlertDialog dialog = new AlertDialog(requireActivity(), "정면을 맞춰주셈");
            dialog.setOnDismissListener(dialogInterface -> {
                // start front analyzer
                // onDetection -> startProcess()
                mCameraHelper.startAnalysis(this::onDetected);
            });
            dialog.show();
        });
        mCameraHelper.setOnTakePictureCallback(this::onTakePicture);
        mCameraHelper.startCamera();

        return mBinding.getRoot();
    }

    private void onDetected() {
        AlertDialog dialog = new AlertDialog(requireActivity(), "5초간 찍을거셈");
        dialog.setOnDismissListener(dialogInterface -> {
            mTakePhotoList = new ArrayList<>();
            mCameraHelper.startProcess(this::onComplete);
        });
        dialog.show();
    }

    private void onTakePicture(Uri uri) {
        mTakePhotoList.add(uri);
    }

    private void onComplete() {
        AlertDialog dialog = new AlertDialog(requireActivity(), "끝났음~~");
        dialog.setOnDismissListener(dialogInterface -> {
            Bundle args = new Bundle(2);
            args.putSerializable(TakePictureViewModel.KEY_SIDE_TYPE, mPhotoType);
            args.putParcelableArrayList(TakePictureViewModel.KEY_SIDE_PHOTO_LIST, mTakePhotoList);
            mViewModel.getNavController().navigate(R.id.action_start_progress, args);
        });
        dialog.show();
    }
}
