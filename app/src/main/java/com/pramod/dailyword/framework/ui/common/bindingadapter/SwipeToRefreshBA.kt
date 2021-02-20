package com.pramod.dailyword.framework.ui.common.bindingadapter

import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.pramod.dailyword.framework.helper.edgetoedge.doOnApplyWindowInsets
import com.pramod.dailyword.framework.util.CommonUtils

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


            appBarLayout?.viewTreeObserver?.addOnGlobalLayoutListener(object :
                OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    appBarLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    val appBarHeight =
                        CommonUtils.pixelToDp(appBarLayout.context, appBarLayout.height.toFloat())
                            .toInt()

                    if (applyTopPaddingInset) {
                        swipeRefreshLayout.doOnApplyWindowInsets { view, windowInsets, initialPadding, initialMargin ->
                            val top = windowInsets.systemWindowInsetTop + appBarHeight
                            swipeRefreshLayout.setProgressViewOffset(true, top, 100 + top)
                        }
                    } else {
                        swipeRefreshLayout.setProgressViewOffset(
                            true,
                            appBarHeight,
                            100 + appBarHeight
                        )

                    }

                }
            })


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