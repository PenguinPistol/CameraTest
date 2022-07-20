package me.penguinpistol.cameratest.v1;

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

import me.penguinpistol.cameratest.R;
import me.penguinpistol.cameratest.v1.camera.PhotoType;
import me.penguinpistol.cameratest.databinding.FragmentTakePictureV1ProgressBinding;

public class TakePictureProgressFragment extends Fragment {

    private FragmentTakePictureV1ProgressBinding mBinding;
    private TakePictureViewModel mViewModel;

    private PhotoType mPhotoType;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(args != null) {
            mPhotoType = (PhotoType) args.getSerializable(TakePictureViewModel.KEY_SIDE_TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentTakePictureV1ProgressBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(requireActivity()).get(TakePictureViewModel.class);

        mViewModel.startSidePhotoAnalysis(requireActivity(), mPhotoType, this::onComplete);

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
