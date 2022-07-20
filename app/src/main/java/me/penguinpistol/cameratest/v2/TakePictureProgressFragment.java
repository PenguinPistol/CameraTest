package me.penguinpistol.cameratest.v2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import me.penguinpistol.cameratest.R;
import me.penguinpistol.cameratest.databinding.FragmentTakePictureV2ProgressBinding;

public class TakePictureProgressFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentTakePictureV2ProgressBinding binding = FragmentTakePictureV2ProgressBinding.inflate(inflater, container, false);
        TakePictureViewModel viewModel = new ViewModelProvider(requireActivity()).get(TakePictureViewModel.class);

        viewModel.startAnalysis(requireActivity(), () -> {
            viewModel.getNavController().navigate(R.id.action_result);
        });

        return binding.getRoot();
    }
}
