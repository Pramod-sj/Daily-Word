package com.pramod.dailyword.util

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

fun Context.getContextCompatColor(colorResId: Int): Int {
    return ContextCompat.getColor(this, colorResId)
}

fun Context.getContextCompatDrawable(drawableResId: Int): Drawable? {
    return ContextCompat.getDrawable(this, drawableResId)
}