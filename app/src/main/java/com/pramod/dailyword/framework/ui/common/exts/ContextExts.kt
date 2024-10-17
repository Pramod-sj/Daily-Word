package com.pramod.dailyword.framework.ui.common.exts

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.core.content.ContextCompat

fun Context.resolveAttrToColor(attr: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

fun Context.resolveAttrToDrawable(attr: Int): Drawable? {
    val typedValue = TypedValue()
    theme.resolveAttribute(attr, typedValue, true)
    return ContextCompat.getDrawable(this, typedValue.resourceId)
}

fun Context.getContextCompatColor(colorResId: Int): Int {
    return try {
        ContextCompat.getColor(this, colorResId)
    } catch (_: Exception) {
        ContextCompat.getColor(this, android.R.color.transparent)
    }
}

fun Context.getContextCompatDrawable(drawableResId: Int): Drawable? {
    return ContextCompat.getDrawable(this, drawableResId)
}