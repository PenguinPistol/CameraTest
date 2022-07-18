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

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.penguinpistol.cameratest.camera.FaceChecker;
import me.penguinpistol.cameratest.camera.FaceDirection;
import me.penguinpistol.cameratest.camera.PhotoType;
import me.penguinpistol.cameratest.camera.SideFaceChecker;
import me.penguinpistol.cameratest.databinding.FragmentTakePictureProgressBinding;

public class TakePictureProgressFragment extends Fragment {

    private FragmentTakePictureProgressBinding mBinding;
    private TakePictureViewModel mViewModel;

    private ArrayList<Uri> mTakePhotoList;
    private PhotoType mPhotoType;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(args != null) {
            mPhotoType = (PhotoType) args.getSerializable(TakePictureViewModel.KEY_SIDE_TYPE);
            mTakePhotoList = args.getParcelableArrayList(TakePictureViewModel.KEY_SIDE_PHOTO_LIST);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentTakePictureProgressBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(requireActivity()).get(TakePictureViewModel.class);

        if(mTakePhotoList == null) {
            mViewModel.getNavController().navigateUp();
            return null;
        }

        mViewModel.startSidePhotoAnalysis(requireActivity(), mPhotoType, mTakePhotoList, this::onComplete);

        return mBinding.getRoot();
    }

    private void onComplete(ArrayList<Uri> list30, ArrayList<Uri> list45) {
        Bundle args = new Bundle(3);
        args.putSerializable(TakePictureViewModel.KEY_SIDE_TYPE, mPhotoType);
        args.putParcelableArrayList("PHOTO_LIST_30", list30);
        args.putParcelableArrayList("PHOTO_LIST_45", list45);

        Log.d("CameraXHelper", "list30 >> " + list30.size() + " / list45 >> " + list45.size());

        mViewModel.getNavController().navigate(R.id.action_progress_complete, args);
    }
}
