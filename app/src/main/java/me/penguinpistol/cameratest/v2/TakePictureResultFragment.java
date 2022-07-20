package me.penguinpistol.cameratest.v2;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.penguinpistol.cameratest.common.ListSpacingDecoration;
import me.penguinpistol.cameratest.databinding.FragmentTakePictureV2ResultBinding;
import me.penguinpistol.cameratest.databinding.ItemTakePictureResult2Binding;
import me.penguinpistol.cameratest.v2.camera.FaceDirection;

public class TakePictureResultFragment extends Fragment {

    private FragmentTakePictureV2ResultBinding mBinding;
    private TakePictureViewModel mViewModel;
    private Adapter mAdapter;
    private AnalysisParameter mParameter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentTakePictureV2ResultBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(requireActivity()).get(TakePictureViewModel.class);
        mParameter = new AnalysisParameter();

        setFaceDirection(FaceDirection.FRONT);

        mBinding.ivFront.setOnClickListener(v -> photoClick(FaceDirection.FRONT));
        mBinding.ivLeft30.setOnClickListener(v -> photoClick(FaceDirection.LEFT_30));
        mBinding.ivLeft45.setOnClickListener(v -> photoClick(FaceDirection.LEFT_45));
        mBinding.ivRight30.setOnClickListener(v -> photoClick(FaceDirection.RIGHT_30));
        mBinding.ivRight45.setOnClickListener(v -> photoClick(FaceDirection.RIGHT_45));

        mBinding.btnUsePhoto.setOnClickListener(v -> {
            FaceDirection dir = mBinding.getCurrentDirection();

            Uri select = mAdapter.getSelectItem();
            mParameter.setImage(dir, select);

            switch (dir) {
                case FRONT:
                    mBinding.ivFront.setImage(select);
                    break;
                case LEFT_30:
                    mBinding.ivLeft30.setImage(select);
                    break;
                case LEFT_45:
                    mBinding.ivLeft45.setImage(select);
                    break;
                case RIGHT_30:
                    mBinding.ivRight30.setImage(select);
                    break;
                case RIGHT_45:
                    mBinding.ivRight45.setImage(select);
                    Toast.makeText(requireActivity(), "선택끝!", Toast.LENGTH_SHORT).show();
                    return;
            }
            FaceDirection next = FaceDirection.values()[Arrays.asList(FaceDirection.values()).indexOf(dir) + 1];
            setFaceDirection(next);
        });

        return mBinding.getRoot();
    }

    private void photoClick(FaceDirection dir) {
        Uri imageUri = mParameter.getImageUri(dir);
        if(imageUri != null) {
            mBinding.ivSelect.setImage(imageUri);
        }
        setFaceDirection(dir);
    }

    private void setFaceDirection(FaceDirection dir) {
        mBinding.setCurrentDirection(dir);
        setList(dir);
    }

    private void setList(FaceDirection direction) {
        mBinding.ivSelect.setImage(null);
        mBinding.btnUsePhoto.setEnabled(false);

        List<Uri> result = mViewModel.getAnalysisResult(direction);
        if(result == null || result.size() == 0) {
            mBinding.tvEmpty.setVisibility(View.VISIBLE);
            mBinding.listPhoto.setVisibility(View.GONE);
        } else {
            mBinding.tvEmpty.setVisibility(View.GONE);
            mBinding.listPhoto.setVisibility(View.VISIBLE);

            mAdapter = new Adapter(result);
            mBinding.listPhoto.setAdapter(mAdapter);
            if (mBinding.listPhoto.getItemDecorationCount() == 0) {
                mBinding.listPhoto.addItemDecoration(new ListSpacingDecoration(8, 20, 20));
                mBinding.listPhoto.setItemAnimator(null);
            }
        }
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.Holder> {
        private final List<Uri> items;
        private int selectPosition;

        public Adapter(List<Uri> items) {
            this.items = items == null ? new ArrayList<>() : items;
            this.selectPosition = -1;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemTakePictureResult2Binding binding = ItemTakePictureResult2Binding.inflate(getLayoutInflater(), parent, false);
            return new Adapter.Holder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull Adapter.Holder holder, int position) {
            int realPosition = holder.getAdapterPosition();
            Glide.with(holder.binding.ivResult).load(items.get(realPosition)).centerCrop().into(holder.binding.ivResult);
            holder.binding.ivSelect.setVisibility(selectPosition == realPosition ? View.VISIBLE : View.GONE);
            holder.binding.getRoot().setClipToOutline(true);
            holder.binding.getRoot().setOnClickListener(v -> {
                if(selectPosition > -1) {
                    notifyItemChanged(selectPosition);
                }
                selectPosition = realPosition;
                notifyItemChanged(realPosition);

                mBinding.ivSelect.setImage(items.get(realPosition));
                mBinding.btnUsePhoto.setEnabled(true);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public Uri getSelectItem() {
            return selectPosition < 0 ? null : items.get(selectPosition);
        }

        class Holder extends RecyclerView.ViewHolder {
            private final ItemTakePictureResult2Binding binding;
            public Holder(@NonNull ItemTakePictureResult2Binding binding) {
                super(binding.getRoot());
                this.binding = binding;
                binding.ivSelect.setVisibility(View.GONE);
            }
        }
    }
}
