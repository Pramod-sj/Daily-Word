package com.pramod.dailyword.framework.ui.common.bindingadapter

import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.pramod.dailyword.framework.ui.common.exts.doOnApplyWindowInsets
import com.pramod.dailyword.framework.prefmanagers.EdgeToEdgePrefManager

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
                appBarLayout.post {
                    appBarLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
                        OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            appBarLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                            val appBarHeight = appBarLayout.height
                            if (EdgeToEdgePrefManager.newInstance(appBarLayout.context)
                                    .isEnabled()
                            ) {
                                recyclerView.doOnApplyWindowInsets { view, windowInsets, initialPadding, initialMargin ->
                                    val topPadding =
                                        appBarHeight + windowInsets.systemWindowInsetTop
                                    recyclerView.setPadding(0, topPadding, 0, 0)
                                }
                            } else {
                                recyclerView.setPadding(0, appBarHeight, 0, 0)
                            }
                        }

                    })

                }
            }
        }

    }
}