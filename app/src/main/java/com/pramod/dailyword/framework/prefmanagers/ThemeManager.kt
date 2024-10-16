package com.pramod.dailyword.framework.prefmanagers

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.pramod.dailyword.framework.util.CommonUtils
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import timber.log.Timber
import javax.inject.Inject


@ActivityScoped
class ThemeManager @Inject constructor(@ActivityContext context: Context) :
    BasePreferenceManager(THEME_PREF, context) {

    private val defaultTheme = THEME_MODE_DARK


    private fun setThemeMode(themeMode: String) {
        editor.putString(KEY_THEME_MODE, themeMode).commit()
    }

    fun getThemeMode(): String {
        return sPrefManager.getString(KEY_THEME_MODE, defaultTheme) ?: defaultTheme
    }

    fun liveData(): LiveData<String> =
        SPrefStringLiveData(sPrefManager, KEY_THEME_MODE, defaultTheme).map {
            return@map it ?: defaultTheme
        }

    fun applyTheme(themeMode: String = getThemeMode()) {
        if (themeMode != getThemeMode()) {
            setThemeMode(themeMode)
        }
        val mode = when (themeMode) {
            THEME_MODE_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            THEME_MODE_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            else -> {
                when {
                    CommonUtils.isAtLeastAndroidP() -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    CommonUtils.isAtLeastAndroidL() -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                    else -> AppCompatDelegate.MODE_NIGHT_NO
                }
            }
        }
        Timber.i("before: applyTheme: " + AppCompatDelegate.getDefaultNightMode())
        AppCompatDelegate.setDefaultNightMode(mode)
        Timber.i("after: applyTheme: " + AppCompatDelegate.getDefaultNightMode())

    }

    private var onThemeValueChangedListener: OnThemeValueChangedListener? = null

    private val sharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, s: String? ->
            if (s == KEY_THEME_MODE) {
                onThemeValueChangedListener?.onThemeValueChanged(
                    sPrefManager.getString(KEY_THEME_MODE, defaultTheme) ?: defaultTheme
                )
            }
        }

    fun registerListener(onThemeValueChangedListener: OnThemeValueChangedListener) {
        this.onThemeValueChangedListener = onThemeValueChangedListener
        sPrefManager.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }

    fun unregisterListener() {
        sPrefManager.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
        this.onThemeValueChangedListener = null
    }

    interface OnThemeValueChangedListener {
        fun onThemeValueChanged(themeMode: String)
    }


    companion object {
        val TAG = ThemeManager::class.java.simpleName


        const val OLD_THEME_PREF = "theme_pref"

        const val THEME_PREF = "app_theme_pref"

        const val KEY_THEME_MODE = "theme_mode"

        const val THEME_MODE_DARK = "Dark"
        const val THEME_MODE_LIGHT = "Light"
        const val THEME_MODE_DEFAULT = "Default"

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
    }
}