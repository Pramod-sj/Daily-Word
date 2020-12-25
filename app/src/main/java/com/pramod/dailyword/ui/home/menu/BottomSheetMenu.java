package com.pramod.dailyword.ui.home.menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.internal.NavigationMenuView;
import com.google.android.material.navigation.NavigationView;

public class BottomSheetMenu extends BottomSheetDialogFragment {

    private NavigationView navigationView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        navigationView = new NavigationView(requireContext());
        ViewGroup.LayoutParams l = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(navigationView, l);
        return container;
    }
}
