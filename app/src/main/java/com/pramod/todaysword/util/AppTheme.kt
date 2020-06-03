package com.pramod.todaysword.util

import android.content.Context
import android.content.res.Configuration

class AppTheme {
    companion object {
        @JvmStatic
        fun isNightModeActive(context: Context): Boolean {
            return when (context.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    true
                }
                else -> false;
            }
        }
    }
}