package com.pramod.dailyword.framework.ui.common.bindingadapter

import android.util.TypedValue
import androidx.core.view.doOnNextLayout
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.pramod.dailyword.framework.ui.common.exts.doOnApplyWindowInsets

class SwipeToRefreshBA {
    companion object {
        @JvmStatic
        @BindingAdapter(
            value = ["app:appBarLayoutForSwipeRefreshLayoutInset", "app:applyTopPaddingInsetForSwipeRefreshLayoutInset"],
            requireAll = false
        )
        fun applyTopOffset(
            swipeRefreshLayout: SwipeRefreshLayout,
            appBarLayout: AppBarLayout?,
            applyTopPaddingInset: Boolean
        ) {
            val appBar = appBarLayout ?: return

            // Use doOnNextLayout (KTX) to avoid memory leaks from GlobalLayoutListeners
            appBar.doOnNextLayout { view ->
                val resources = view.resources
                val appBarHeightPx = view.height

                // Convert 64dp (standard SRR distance) to Pixels based on device density
                val defaultOptionOffsetPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    64f,
                    resources.displayMetrics
                ).toInt()

                if (applyTopPaddingInset) {
                    swipeRefreshLayout.doOnApplyWindowInsets { _, insets, _, _ ->

                        val end = appBarHeightPx + defaultOptionOffsetPx

                        swipeRefreshLayout.setProgressViewOffset(true, appBarHeightPx, end)
                    }
                } else {
                    val end = appBarHeightPx + defaultOptionOffsetPx
                    swipeRefreshLayout.setProgressViewOffset(true, appBarHeightPx, end)
                }
            }
        }


        @JvmStatic
        @BindingAdapter("app:showSwipeToRefreshProgress")
        fun showProgress(
            swipeRefreshLayout: SwipeRefreshLayout,
            showSwipeToRefreshProgress: Boolean
        ) {
            swipeRefreshLayout.post {
                swipeRefreshLayout.isRefreshing = showSwipeToRefreshProgress
            }
        }
    }
}