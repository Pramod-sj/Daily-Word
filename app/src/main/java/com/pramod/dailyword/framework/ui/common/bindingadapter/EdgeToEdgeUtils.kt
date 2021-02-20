package com.pramod.dailyword.framework.ui.common.bindingadapter

import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.databinding.BindingAdapter
import com.pramod.dailyword.framework.helper.edgetoedge.*

class EdgeToEdgeUtils {
    companion object {


        @BindingAdapter("app:applyTopBottomPaddingInset")
        @JvmStatic
        fun applyTopBottomPaddingInset(view: View, applyBottomInset: Boolean) {
            if (applyBottomInset) {
                view.doOnApplyWindowInsets { view, windowInsets, padding, margin ->
                    view.updatePadding(
                        top = padding.top + windowInsets.systemWindowInsetTop,
                        bottom = padding.bottom + windowInsets.systemWindowInsetBottom
                    )
                }
            }
        }

        @BindingAdapter("app:applyTopBottomMarginInset")
        @JvmStatic
        fun applyTopBottomMarginInset(view: View, applyBottomInset: Boolean) {
            if (applyBottomInset) {
                view.doOnApplyWindowInsets { view, windowInsets, padding, margin ->
                    view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        setMargins(
                            margin.left,
                            margin.top + windowInsets.systemWindowInsetTop,
                            margin.right,
                            margin.bottom + windowInsets.systemWindowInsetBottom
                        )
                    }
                }
            }
        }

        @BindingAdapter("app:applyBottomPaddingInset")
        @JvmStatic
        fun applyBottomPaddingInsetOnView(view: View, applyBottomInset: Boolean) {
            if (applyBottomInset) {
                view.applyNavigationBarPaddingInsets()
            }
        }

        @BindingAdapter("app:applyTopPaddingInset")
        @JvmStatic
        fun applyAppBarInset(view: View, applyBottomInset: Boolean) {
            if (applyBottomInset) {
                view.applySystemBarPaddingInsets()
            }
        }


        @BindingAdapter("app:applyTopMarginInset")
        @JvmStatic
        fun applyMarginInsetToTopView(view: View, applyBottomInset: Boolean) {
            if (applyBottomInset) {
                view.applyTopViewMarginInsets()
            }
        }


        @BindingAdapter("app:applyBottomMarginInset")
        @JvmStatic
        fun applyMarginInsetToBottomView(view: View, applyBottomInset: Boolean) {
            if (applyBottomInset) {
                view.applyNavigationAndBottomNavigationViewMarginInsets()
            }
        }

    }
}