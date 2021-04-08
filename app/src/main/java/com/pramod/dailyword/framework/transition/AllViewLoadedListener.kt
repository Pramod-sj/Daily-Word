package com.pramod.dailyword.framework.transition

import android.view.View
import android.view.ViewTreeObserver

/**
 * skip null views
 */
fun doOnViewLoaded(vararg views: View?, loadedCallback: () -> Unit) {
    val viewLoadTrack = hashMapOf<Int, Boolean>()
    views.forEachIndexed { index, view ->
        if (view != null) {
            viewLoadTrack[index] = false
            view.viewTreeObserver?.addOnGlobalLayoutListener(object :
                CustomOnGlobalLayoutListener(index, view) {
                override fun onGlobalLayout() {
                    super.onGlobalLayout()
                    viewLoadTrack[viewIndex] = true
                    if (viewLoadTrack.values.all { it }) {
                        loadedCallback.invoke()
                    }
                }
            })
        } else {
            viewLoadTrack[index] = true
        }
    }
}

fun doOnViewPreDrawn(vararg views: View, preDrawnCallback: () -> Unit) {
    val viewLoadTrack = hashMapOf<Int, Boolean>()
    views.forEachIndexed { index, view ->
        viewLoadTrack[index] = false
        view.viewTreeObserver.addOnPreDrawListener(object :
            CustomOnPreDrawListener(index, view) {
            override fun onPreDraw(): Boolean {
                viewLoadTrack[viewIndex] = true
                if (viewLoadTrack.values.all { it }) {
                    preDrawnCallback.invoke()
                }
                return super.onPreDraw()
            }
        })
    }
}

abstract class CustomOnGlobalLayoutListener(val viewIndex: Int, val view: View) :
    ViewTreeObserver.OnGlobalLayoutListener {
    override fun onGlobalLayout() {
        view.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

}

abstract class CustomOnPreDrawListener(val viewIndex: Int, val view: View) :
    ViewTreeObserver.OnPreDrawListener {
    override fun onPreDraw(): Boolean {
        view.viewTreeObserver.removeOnPreDrawListener(this)
        return true
    }

}
