package com.pramod.dailyword.framework.ui.common.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

class SafeTouch @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {
    //region fix interfering with compose drawer when swipe horizontally or vertically
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN ->
                parent.requestDisallowInterceptTouchEvent(true)

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP ->
                parent.requestDisallowInterceptTouchEvent(false)
        }
        return false
    }
    //endregion

}