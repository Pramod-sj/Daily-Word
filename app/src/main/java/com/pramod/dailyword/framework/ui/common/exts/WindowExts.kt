package com.pramod.dailyword.framework.ui.common.exts

import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.pramod.dailyword.R

fun Window.configStatusBar(
    makeLight: Boolean,
    statusBarColorResId: Int = -1,
    matchingBackgroundColor: Boolean = false
) {
    val oldFlags = decorView.systemUiVisibility
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        var flags = oldFlags
        flags = if (makeLight) {
            flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        decorView.systemUiVisibility = flags
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        statusBarColor = if (matchingBackgroundColor) {
            context.resolveAttrToColor(android.R.attr.windowBackground)
        } else {
            context.getContextCompatColor(
                if (statusBarColorResId != -1) statusBarColorResId
                else (if (makeLight) R.color.white else R.color.black)
            )
        }
    } else {
        statusBarColor = context.getContextCompatColor(R.color.black)
    }
}