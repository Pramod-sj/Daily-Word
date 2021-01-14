package com.pramod.dailyword

import android.view.ViewTreeObserver
import androidx.core.view.updatePadding
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout

class RecyclerViewBA {
    companion object {

        @JvmStatic
        @BindingAdapter("app:applyRecyclerViewTopPadding", "app:appBarLayout")
        fun applyRecyclerViewTopPadding(
            recyclerView: RecyclerView,
            applyRecyclerViewTopPadding: Boolean = false,
            appBarLayout: AppBarLayout
        ) {
            if (applyRecyclerViewTopPadding) {
                appBarLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        appBarLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        val appBarHeight = appBarLayout.height
                        recyclerView.updatePadding(top = appBarHeight)
                    }

                })
            }
        }

    }
}