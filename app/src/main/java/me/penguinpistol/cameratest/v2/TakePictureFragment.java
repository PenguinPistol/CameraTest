package me.penguinpistol.cameratest.v2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import me.penguinpistol.cameratest.databinding.FragmentTakePictureV2Binding;

public class TakePictureFragment extends Fragment {

    private FragmentTakePictureV2Binding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentTakePictureV2Binding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }
}
