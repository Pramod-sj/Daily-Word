package com.pramod.dailyword.helper

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.pramod.dailyword.util.CommonUtils

class ThemeManager {
    enum class Options {
        DARK,
        LIGHT,
        DEFAULT
    }

    companion object {
        @JvmStatic
        fun isNightModeActive(context: Context): Boolean {
            return when (context.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    true
                }
                else -> false
            }
        }

        fun getDefaultThemeMode(): Int = AppCompatDelegate.getDefaultNightMode()

        fun getDefaultThemeOption(): Options {
            return when (AppCompatDelegate.getDefaultNightMode()) {
                AppCompatDelegate.MODE_NIGHT_YES -> Options.DARK
                AppCompatDelegate.MODE_NIGHT_NO -> Options.LIGHT
                else -> Options.DEFAULT
            }
        }

        fun applyTheme(option: Options) {
            val mode = when (option) {
                Options.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                Options.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                else -> {
                    when {
                        CommonUtils.isAtLeastAndroidP() -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        CommonUtils.isAtLeastAndroidL() -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                        else -> AppCompatDelegate.MODE_NIGHT_NO

                    }
                }
            }
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }
}