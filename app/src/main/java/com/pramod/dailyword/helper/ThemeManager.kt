package com.pramod.dailyword.helper

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import com.pramod.dailyword.util.CommonUtils

class ThemeManager private constructor(val context: Context) {
    private val sPref = context.getSharedPreferences(THEME_PREF, Context.MODE_PRIVATE)
    private val editor = sPref.edit()
    private val defaultTheme = Options.DARK

    enum class Options {
        DARK,
        LIGHT,
        DEFAULT
    }

    fun setDefaultThemeMode(option: Options) =
        editor.putInt(KEY_THEME_MODE, option.ordinal).commit()


    fun getDefaultThemeModeOption() =
        Options.values()[sPref.getInt(KEY_THEME_MODE, defaultTheme.ordinal)]

    fun getDefaultThemeMode() = sPref.getInt(KEY_THEME_MODE, defaultTheme.ordinal)

    fun liveData(): SPrefIntLiveData =
        SPrefIntLiveData(sPref, KEY_THEME_MODE, defaultTheme.ordinal)

    fun applyTheme(option: Options = getDefaultThemeModeOption()) {
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

    companion object {
        const val THEME_PREF = "theme_pref"

        const val KEY_THEME_MODE = "theme_mode"

        @JvmStatic
        fun newInstance(context: Context): ThemeManager = ThemeManager(context)

        @JvmStatic
        fun isNightModeActive(context: Context): Boolean {
            return when (context.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    true
                }
                else -> false
            }
        }

        @JvmStatic
        fun getThemeNameFromOrdinal(ordinal: Int): String = Options.values()[ordinal].name

        /*fun getDefaultThemeMode(): Int = AppCompatDelegate.getDefaultNightMode()

        fun getDefaultThemeOption(): Options {
            return when (AppCompatDelegate.getDefaultNightMode()) {
                AppCompatDelegate.MODE_NIGHT_YES -> Options.DARK
                AppCompatDelegate.MODE_NIGHT_NO -> Options.LIGHT
                else -> Options.DEFAULT
            }
        }

        */
    }
}