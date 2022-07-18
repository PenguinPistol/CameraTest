package me.penguinpistol.cameratest;

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

import me.penguinpistol.cameratest.camera.PhotoType;
import me.penguinpistol.cameratest.databinding.FragmentTakePictureResultSideBinding;
import me.penguinpistol.cameratest.databinding.ItemTakePictureResultBinding;

public class TakePictureResultSideFragment extends Fragment {

    private FragmentTakePictureResultSideBinding mBinding;
    private TakePictureViewModel mViewModel;
    private PhotoType mPhotoType;
    private ArrayList<Uri> mPhotoList30;
    private ArrayList<Uri> mPhotoList45;

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
        mBinding = FragmentTakePictureResultSideBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(requireActivity()).get(TakePictureViewModel.class);

        mBinding.btnRetake.setOnClickListener(v -> mViewModel.getNavController().navigateUp());
        mBinding.btnNext.setOnClickListener(v -> {});

        initList(mBinding.listPhoto30, mPhotoList30);
        initList(mBinding.listPhoto45, mPhotoList45);

        return mBinding.getRoot();
    }

    private void initList(RecyclerView view, List<Uri> items) {
        view.setAdapter(new Adapter(items));
        if(view.getItemDecorationCount() == 0) {
            view.addItemDecoration(new ListSpacingDecoration(8));
        }
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
            Glide.with(holder.itemView).load(items.get(realPosition)).centerCrop().into(holder.binding.ivResult);
            holder.binding.ivResult.setClipToOutline(true);
            holder.binding.setIsSelect(selectPosition == realPosition);
            holder.itemView.setOnClickListener(v -> {
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
