package com.pramod.todaysword.helper

import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.pramod.todaysword.util.CommonUtils

class ThemeManager {
    enum class Options {
        DARK,
        LIGHT,
        DEFAULT
    }

    companion object {

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