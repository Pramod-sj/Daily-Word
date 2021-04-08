package com.pramod.dailyword.framework.ui.common.bindingadapter

import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.databinding.BindingAdapter
import kotlin.math.roundToInt

class ViewUtils {
    companion object {

        @JvmStatic
        fun calculateActionBarHeight(context: Context): Int {
            val typedValue = TypedValue()
            if (context.theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
                return TypedValue.complexToDimensionPixelSize(
                    typedValue.data,
                    context.resources.displayMetrics
                )
            }
            return 0
        }


        @BindingAdapter("app:isGone")
        @JvmStatic
        fun isGone(view: View, isGone: Boolean) {
            if (isGone) {
                view.visibility = View.GONE
            } else {
                view.visibility = View.VISIBLE
            }
        }

        @BindingAdapter("app:isVisible")
        @JvmStatic
        fun isVisible(view: View, isVisible: Boolean) {
            view.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        }


        @JvmStatic
        @BindingAdapter("app:paddingBottom")
        fun setPaddingBottom(view: View, dimen: Float) {
            Log.i("TAG", "setPaddingBottom: $dimen")
            view.updatePadding(
                bottom = dimen.roundToInt()
            )
        }

        @JvmStatic
        @BindingAdapter("app:paddingTop")
        fun setPaddingTop(view: View, dimen: Float) {
            view.updatePadding(
                top = dimen.toInt()
            )
        }

        @JvmStatic
        @BindingAdapter("app:layout_marginBottom")
        fun setLayoutMarginBottom(view: View, dimen: Float) {
            (view.layoutParams as ViewGroup.MarginLayoutParams).let {
                it.bottomMargin = dimen.toInt()
                view.layoutParams = it
            }
        }

        @JvmStatic
        @BindingAdapter("app:layout_marginTop")
        fun setLayoutMarginTop(view: View, dimen: Float) {
            (view.layoutParams as ViewGroup.MarginLayoutParams).let {
                it.topMargin = dimen.toInt()
                view.layoutParams = it
            }
        }

        @JvmStatic
        @BindingAdapter("app:layout_marginStart")
        fun setLayoutMarginStart(view: View, dimen: Float) {
            (view.layoutParams as ViewGroup.MarginLayoutParams).let {
                it.marginStart = dimen.toInt()
                view.layoutParams = it
            }
        }

        @JvmStatic
        @BindingAdapter("app:layout_marginEnd")
        fun setLayoutMarginEnd(view: View, dimen: Float) {
            (view.layoutParams as ViewGroup.MarginLayoutParams).let {
                it.marginEnd = dimen.toInt()
                view.layoutParams = it
            }
        }

    }
}