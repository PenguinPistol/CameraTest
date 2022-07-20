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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import me.penguinpistol.cameratest.R;
import me.penguinpistol.cameratest.v1.camera.FaceDirection;
import me.penguinpistol.cameratest.v1.camera.PhotoType;
import me.penguinpistol.cameratest.common.AlertDialog;
import me.penguinpistol.cameratest.common.ListSpacingDecoration;
import me.penguinpistol.cameratest.databinding.FragmentTakePictureV1ResultSideBinding;
import me.penguinpistol.cameratest.databinding.ItemTakePictureResultBinding;

public class TakePictureResultSideFragment extends Fragment {

    private FragmentTakePictureV1ResultSideBinding mBinding;
    private TakePictureViewModel mViewModel;
    private PhotoType mPhotoType;
    private ArrayList<Uri> mPhotoList30;
    private ArrayList<Uri> mPhotoList45;
    private Adapter mListAdapter30;
    private Adapter mListAdapter45;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if(args != null) {
            mPhotoType = (PhotoType) args.getSerializable(TakePictureViewModel.KEY_SIDE_TYPE);
            mPhotoList30 = args.getParcelableArrayList("PHOTO_LIST_30");
            mPhotoList45 = args.getParcelableArrayList("PHOTO_LIST_45");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentTakePictureV1ResultSideBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(requireActivity()).get(TakePictureViewModel.class);

        mBinding.setPhotoType(mPhotoType);
        mBinding.btnRetake.setOnClickListener(v -> {
            Bundle args = new Bundle(1);
            args.putSerializable(TakePictureViewModel.KEY_SIDE_TYPE, mPhotoType);
            mViewModel.getNavController().navigate(R.id.action_next_side, args);
        });
        mBinding.btnNext.setOnClickListener(v -> {
            final int select30 = mListAdapter30.getSelectItem();
            final int select45 = mListAdapter45.getSelectItem();

            if(select30 < 0 || select45 < 0) {
                new AlertDialog(requireActivity(), "30도, 45도 사진에서 하나씩 선택해 주세요.").show();
                return;
            }

            mViewModel.setParameterImage(mPhotoType == PhotoType.LEFT ? FaceDirection.LEFT_30 : FaceDirection.RIGHT_30, mPhotoList30.get(select30));
            mViewModel.setParameterImage(mPhotoType == PhotoType.LEFT ? FaceDirection.LEFT_45 : FaceDirection.RIGHT_45, mPhotoList45.get(select45));

            // 오른쪽 촬영
            if(mPhotoType == PhotoType.RIGHT) {
                // 최종확인 결과로 이동..
                mViewModel.getNavController().navigate(R.id.action_result_final);
            } else {
                Bundle args = new Bundle(1);
                args.putSerializable(TakePictureViewModel.KEY_SIDE_TYPE, PhotoType.RIGHT);
                mViewModel.getNavController().navigate(R.id.action_next_side, args);
            }
        });

        mListAdapter30 = initList(mBinding.listPhoto30, mBinding.tvEmpty30, mPhotoList30);
        mListAdapter45 = initList(mBinding.listPhoto45, mBinding.tvEmpty45, mPhotoList45);

        return mBinding.getRoot();
    }

    private Adapter initList(RecyclerView listView, View emptyView, List<Uri> items) {
        Adapter adapter;
        if(items == null || items.size() == 0) {
            listView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            adapter = new Adapter(new ArrayList<>());
        } else {
            adapter = new Adapter(items);
            listView.setAdapter(adapter);
            listView.setItemAnimator(null);
            if (listView.getItemDecorationCount() == 0) {
                listView.addItemDecoration(new ListSpacingDecoration(8, 20, 20));
            }
        }
        return adapter;
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.Holder> {
        private final List<Uri> items;
        private int selectPosition;

        public Adapter(List<Uri> items) {
            this.items = items;
            this.selectPosition = -1;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemTakePictureResultBinding binding = ItemTakePictureResultBinding.inflate(getLayoutInflater(), parent, false);
            return new Holder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            int realPosition = holder.getAdapterPosition();
            Glide.with(holder.binding.ivResult).load(items.get(realPosition)).centerCrop().into(holder.binding.ivResult);
            holder.binding.setIsSelect(selectPosition == realPosition);
            holder.binding.getRoot().setClipToOutline(true);
            holder.binding.getRoot().setOnClickListener(v -> {
                if(selectPosition > -1) {
                    notifyItemChanged(selectPosition);
                }
                selectPosition = realPosition;
                notifyItemChanged(realPosition);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        // TODO item type 변경
        public int getSelectItem() {
            return selectPosition;
        }

        class Holder extends RecyclerView.ViewHolder {
            private final ItemTakePictureResultBinding binding;
            public Holder(@NonNull ItemTakePictureResultBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
