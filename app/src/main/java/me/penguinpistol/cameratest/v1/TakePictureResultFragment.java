package me.penguinpistol.cameratest.v1;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import me.penguinpistol.cameratest.camera.FaceDirection;
import me.penguinpistol.cameratest.common.AnalysisParameter;
import me.penguinpistol.cameratest.databinding.FragmentTakePictureV1ResultBinding;

public class TakePictureResultFragment extends Fragment {

    private FragmentTakePictureV1ResultBinding mBinding;
    private TakePictureViewModel mViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentTakePictureV1ResultBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(requireActivity()).get(TakePictureViewModel.class);

        AnalysisParameter parameter = mViewModel.getAnalysisParameter();

        setSelectImage(parameter.getImageUri(FaceDirection.FRONT));
        mBinding.ivSelect.setClipToOutline(true);

        initPhotoSet(mBinding.ivLeft45, parameter.getImageUri(FaceDirection.LEFT_45));
        initPhotoSet(mBinding.ivLeft30, parameter.getImageUri(FaceDirection.LEFT_30));
        initPhotoSet(mBinding.ivFront, parameter.getImageUri(FaceDirection.FRONT));
        initPhotoSet(mBinding.ivRight30, parameter.getImageUri(FaceDirection.RIGHT_30));
        initPhotoSet(mBinding.ivRight45, parameter.getImageUri(FaceDirection.RIGHT_45));

        mBinding.btnFinish.setOnClickListener(v -> requireActivity().finish());

        return mBinding.getRoot();
    }

    private void initPhotoSet(ImageView imageView, Uri uri) {
        Glide.with(requireActivity()).load(uri).centerCrop().into(imageView);
        imageView.setClipToOutline(true);
        imageView.setOnClickListener(v -> setSelectImage(uri));
    }

    private void setSelectImage(Uri uri) {
        Glide.with(requireActivity()).load(uri).centerCrop().into(mBinding.ivSelect);
    }
}
