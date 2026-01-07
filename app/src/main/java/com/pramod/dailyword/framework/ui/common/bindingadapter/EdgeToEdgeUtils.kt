package com.pramod.dailyword.framework.ui.common.bindingadapter

import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.view.updatePadding
import androidx.databinding.BindingAdapter
import com.pramod.dailyword.framework.ui.common.exts.doOnApplyWindowInsets

class EdgeToEdgeUtils {
    companion object {


        @JvmStatic
        @BindingAdapter(
            value = [
                "app:applyVerticalPaddingInset",
                "app:applyVerticalMarginInset",
            ], requireAll = false
        )
        fun applyVerticalInset(
            view: View,
            applyVerticalPaddingInset: Boolean,
            applyVerticalMarginInset: Boolean
        ) {
            view.doOnApplyWindowInsets { view, windowInsets, initialPadding, initialMargin ->
                view.updatePadding(
                    top = if (applyVerticalPaddingInset) windowInsets.systemWindowInsetTop + initialPadding.top
                    else initialPadding.top,
                    bottom = if (applyVerticalPaddingInset) windowInsets.systemWindowInsetBottom + initialPadding.bottom
                    else initialPadding.bottom
                )
            }
        }

        @JvmStatic
        @BindingAdapter(
            value = [
                "app:applyTopPaddingInset",
                "app:applyTopMarginInset",
            ], requireAll = false
        )
        fun applyTopInset(
            view: View,
            applyTopPaddingInset: Boolean,
            applyTopMarginInset: Boolean,
        ) {
            view.doOnApplyWindowInsets { view, windowInsets, initialPadding, initialMargin ->
                view.updatePadding(
                    top = if (applyTopPaddingInset) windowInsets.systemWindowInsetTop + initialPadding.top
                    else initialPadding.top,
                    bottom = initialPadding.bottom
                )

/*                    view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        setMargins(
                            initialMargin.left,
                            if (applyVerticalMarginInset || applyTopMarginInset) windowInsets.systemWindowInsetTop else initialMargin.top,
                            initialMargin.right,
                            if (applyVerticalMarginInset || applyBottomMarginInset) windowInsets.systemWindowInsetTop else initialMargin.bottom
                        )
                    }*/

            }
        }

        @JvmStatic
        @BindingAdapter(
            value = [
                "app:applyBottomPaddingInset",
                "app:applyBottomMarginInset",
            ], requireAll = false
        )
        fun applyBottomInset(
            view: View,
            applyBottomPaddingInset: Boolean,
            applyBottomMarginInset: Boolean
        ) {

            view.doOnApplyWindowInsets { view, windowInsets, initialPadding, initialMargin ->

                view.updatePadding(
                    top = initialPadding.top,
                    bottom = if (applyBottomPaddingInset) windowInsets.systemWindowInsetBottom + initialPadding.bottom
                    else initialPadding.bottom
                )
            }
        }


        @JvmStatic
        @BindingAdapter(
            value = [
                "app:applyContentBottomPaddingInset"
            ], requireAll = false
        )
        fun applyBottomInset(
            cardView: CardView,
            applyContentBottomPaddingInset: Boolean
        ) {

            cardView.doOnApplyWindowInsets { view, windowInsets, initialPadding, initialMargin ->

                cardView.setContentPadding(
                    initialPadding.left,
                    initialPadding.top,
                    initialPadding.right,
                    if (applyContentBottomPaddingInset) windowInsets.systemWindowInsetBottom + initialPadding.bottom
                    else initialPadding.bottom
                )
            }
        }


    }
}