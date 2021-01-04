package com.pramod.dailyword

import android.view.ViewTreeObserver
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.pramod.dailyword.helper.edgetoedge.doOnApplyWindowInsets
import kotlinx.android.synthetic.main.activity_word_list.*

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
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    appBarLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    val appBarHeight = appBarLayout.height

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
    }
}