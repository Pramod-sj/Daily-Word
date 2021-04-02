package com.pramod.dailyword.framework.ui.common.bindingadapter

import android.content.Context
import android.util.TypedValue
import android.view.View
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.databinding.BindingAdapter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView

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


    }
}