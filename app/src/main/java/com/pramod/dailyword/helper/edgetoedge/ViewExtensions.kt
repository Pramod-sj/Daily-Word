package com.pramod.dailyword.helper.edgetoedge


import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

fun View.applySystemBarPaddingInsets() {
    this.doOnApplyWindowInsets { view, insets, padding, _ ->
        Log.i(TAG, "applySystemBarPaddingInsets: ")
        view.updatePadding(top = padding.top + insets.systemWindowInsetTop)
    }
}

fun View.applyNavigationBarPaddingInsets() {
    this.doOnApplyWindowInsets { view, insets, padding, _ ->
        Log.i(TAG, "applyNavigationBarPaddingInsets: ")
        view.updatePadding(bottom = padding.bottom + insets.systemWindowInsetBottom)
    }
}


fun View.applyTopViewMarginInsets() {
    this.doOnApplyWindowInsets { view, insets, _, margin ->
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(
                margin.left,
                margin.top + insets.systemWindowInsetTop,
                margin.right,
                margin.bottom
            )
        }
    }
}

fun View.applyNavigationAndBottomNavigationViewMarginInsets() {
    this.doOnApplyWindowInsets { view, insets, _, margin ->
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(
                margin.left,
                margin.top,
                margin.right,
                margin.bottom + insets.systemWindowInsetBottom
            )
        }
    }
}

fun View.applyVerticalInsets() {
    this.doOnApplyWindowInsets { view, windowInsets, initialPadding, _ ->
        view.updatePadding(
            top = initialPadding.top + windowInsets.systemWindowInsetTop,
            bottom = initialPadding.bottom + windowInsets.systemWindowInsetBottom
        )
    }
}

/*********************
 * Implementation(s) *
 *********************/

const val TAG = "Debug"

fun View.doOnApplyWindowInsets(f: (View, WindowInsets, InitialPadding, InitialMargin) -> Unit) {
    Log.i(TAG, "doOnApplyWindowInsets: ")
    // Create a snapshot of the view's padding/margin state
    val initialPadding = recordInitialPaddingForView(this)
    val initialMargin = recordInitialMarginForView(this)
    // Set an actual OnApplyWindowInsetsListener which proxies to the given
    // lambda, also passing in the original padding state
    setOnApplyWindowInsetsListener { v, insets ->
        insets.consumeSystemWindowInsets()
        f(v, insets, initialPadding, initialMargin)
        // Always return the insets, so that children can also use them
        insets
    }
    // request some insets
    requestApplyInsetsWhenAttached()
}

class InitialPadding(
    val left: Int, val top: Int,
    val right: Int, val bottom: Int
)

class InitialMargin(
    val left: Int, val top: Int,
    val right: Int, val bottom: Int
)

private fun recordInitialPaddingForView(view: View) =
    InitialPadding(view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom)

private fun recordInitialMarginForView(view: View) =
    InitialMargin(
        (view.layoutParams as? ViewGroup.MarginLayoutParams)?.leftMargin ?: 0,
        (view.layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin ?: 0,
        (view.layoutParams as? ViewGroup.MarginLayoutParams)?.rightMargin ?: 0,
        (view.layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0
    )

private fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        // We're already attached, just request as normal
        requestApplyInsets()
    } else {
        // We're not attached to the hierarchy, add a listener to
        // request when we are
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}