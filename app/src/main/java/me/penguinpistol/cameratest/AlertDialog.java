package me.penguinpistol.cameratest;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import me.penguinpistol.cameratest.databinding.DialogAlertBinding;

public class AlertDialog extends Dialog {

    private final String message;

    public AlertDialog(@NonNull Activity activity, String message) {
        super(activity);
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        DialogAlertBinding binding = DialogAlertBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvMessage.setText(message);
        binding.btnPositive.setOnClickListener(v -> dismiss());
    }

    @Override
    public void show() {
        super.show();
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }
}
