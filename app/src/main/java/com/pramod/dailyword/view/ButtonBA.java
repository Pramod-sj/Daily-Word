package com.pramod.dailyword.view;

import android.content.res.ColorStateList;

import androidx.databinding.BindingAdapter;

import com.google.android.material.button.MaterialButton;

public class ButtonBA {
    @BindingAdapter("applyBackgroundTint")
    public static void applyBackgroundTint(MaterialButton materialButton, int color) {
        materialButton.setBackgroundTintList(ColorStateList.valueOf(color));
    }
}
