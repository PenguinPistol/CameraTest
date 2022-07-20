package me.penguinpistol.cameratest.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import me.penguinpistol.cameratest.R;
import me.penguinpistol.cameratest.databinding.ViewPhotoBinding;

public class PhotoView extends MaterialCardView {

    private final ViewPhotoBinding mBinding;

    public PhotoView(@NonNull Context context) {
        this(context, null, 0);
    }

    public PhotoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float density = getResources().getDisplayMetrics().density;

        mBinding = ViewPhotoBinding.inflate(LayoutInflater.from(context), this);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PhotoView);

        mBinding.text.setText(ta.getText(R.styleable.PhotoView_android_text));

        ta.recycle();

        setCardElevation(0);
        setRadius(10 * density);
        setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.light_gray));
    }

    public void setImage(Uri uri) {
        Glide.with(this).load(uri).centerCrop().into(mBinding.image);
    }
}
