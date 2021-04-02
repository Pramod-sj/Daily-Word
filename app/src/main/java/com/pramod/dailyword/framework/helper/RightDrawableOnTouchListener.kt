package com.pramod.dailyword.framework.helper

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.widget.TextView

abstract class RightDrawableOnTouchListener(view: TextView) : View.OnTouchListener {
    var drawable: Drawable? = null
    private val fuzz = 10

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
     */
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && drawable != null) {
            val x = event.x
            val y = event.y
            val bounds: Rect = (drawable as Drawable).bounds
            if (x >= v.right - bounds.width() - fuzz && x <= v.right - v.paddingRight + fuzz && y >= v.paddingTop - fuzz && y <= v.height - v.paddingBottom + fuzz) {
                return onDrawableTouch(event)
            }
        }
        return false
    }

    abstract fun onDrawableTouch(event: MotionEvent?): Boolean

    /**
     * @param keyword
     */
    init {
        val drawables: Array<Drawable> = view.getCompoundDrawables()
        if (drawables.size == 4) drawable = drawables[2]
    }
}