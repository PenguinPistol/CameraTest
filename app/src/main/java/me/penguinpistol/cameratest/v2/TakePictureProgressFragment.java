package me.penguinpistol.cameratest.v2;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import me.penguinpistol.cameratest.R;
import me.penguinpistol.cameratest.common.AlertDialog;
import me.penguinpistol.cameratest.databinding.FragmentTakePictureV2ProgressBinding;
import me.penguinpistol.cameratest.v2.camera.FaceDirection;

public class TakePictureProgressFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentTakePictureV2ProgressBinding binding = FragmentTakePictureV2ProgressBinding.inflate(inflater, container, false);
        TakePictureViewModel viewModel = new ViewModelProvider(requireActivity()).get(TakePictureViewModel.class);

        viewModel.startAnalysis(requireActivity(), () -> {
            List<FaceDirection> emptyDirections = new ArrayList<>();
            for(FaceDirection dir : FaceDirection.values()) {
                List<Uri> result = viewModel.getAnalysisResult(dir);
                if(result == null || result.size() == 0) {
                    emptyDirections.add(dir);
                }
            }
            if(emptyDirections.size() > 0) {
                String message = TextUtils.join(" / ", emptyDirections.stream().map(FaceDirection::getLabel).collect(Collectors.toList()));
                AlertDialog dialog = new AlertDialog(requireActivity(), message + "\n각도에서 감지된 사진이 없습니다.\n확인 버튼을 누르시면 촬영 화면으로 돌아갑니다.");
                dialog.setOnDismissListener(dialogInterface -> {
                    viewModel.removeAllPhoto();
                    viewModel.getNavController().navigateUp();
                });
                dialog.show();
            } else {
                viewModel.getNavController().navigate(R.id.action_analysis_finish);
            }
        });

        return binding.getRoot();
    }
}
