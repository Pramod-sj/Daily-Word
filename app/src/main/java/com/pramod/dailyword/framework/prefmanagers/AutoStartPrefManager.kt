package com.pramod.dailyword.framework.prefmanagers

import android.content.Context
import javax.inject.Inject

class AutoStartPrefManager @Inject constructor(context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    fun clickedOnAlreadyEnabled() {
        editor.putBoolean(KEY_CLICK_ON_ALREADY_ENABLED, true).commit()
    }

    fun clickedOnSetting() {
        editor.putBoolean(KEY_CLICK_ON_SETTING, true).commit()
    }

    fun isAutoStartAlreadyEnabled() =
        sharedPreferences.getBoolean(KEY_CLICK_ON_ALREADY_ENABLED, false)

    fun isClickedOnSetting() = sharedPreferences.getBoolean(KEY_CLICK_ON_SETTING, false)

    companion object {
        private const val PREFERENCES_NAME = "auto_start_pref"
        private const val KEY_CLICK_ON_ALREADY_ENABLED = "click_on_already_enabled"
        private const val KEY_CLICK_ON_SETTING = "click_on_setting"
        fun newInstance(context: Context) = AutoStartPrefManager(context)
    }

}