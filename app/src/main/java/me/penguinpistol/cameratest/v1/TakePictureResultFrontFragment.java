package me.penguinpistol.cameratest.v1;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import java.util.Arrays;

import me.penguinpistol.cameratest.R;
import me.penguinpistol.cameratest.camera.FaceDirection;
import me.penguinpistol.cameratest.camera.PhotoType;
import me.penguinpistol.cameratest.databinding.FragmentTakePictureV1ResultFrontBinding;

public class TakePictureResultFrontFragment extends Fragment {

    private FragmentTakePictureV1ResultFrontBinding mBinding;
    private TakePictureViewModel mViewModel;
    private Uri mImageUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(args != null) {
            mImageUri = args.getParcelable("uri");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentTakePictureV1ResultFrontBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(requireActivity()).get(TakePictureViewModel.class);

        if(mImageUri == null) {
            mViewModel.getNavController().navigateUp();
            return null;
        }

        mBinding.ivResult.setClipToOutline(true);
        Glide.with(mBinding.ivResult).load(mImageUri).centerCrop().into(mBinding.ivResult);

        mBinding.btnRetake.setOnClickListener(v -> {
            mViewModel.removeFile(mImageUri);
            mViewModel.getNavController().navigateUp();
        });
        mBinding.btnNext.setOnClickListener(v -> {
            mViewModel.setParameterImage(FaceDirection.FRONT, mImageUri);
            mViewModel.setTakePhoto(PhotoType.FRONT, Arrays.asList(mImageUri));

            Bundle args = new Bundle(1);
            args.putSerializable(TakePictureViewModel.KEY_SIDE_TYPE, PhotoType.LEFT);
            mViewModel.getNavController().navigate(R.id.action_take_side, args);
        });

        return mBinding.getRoot();
    }
}
