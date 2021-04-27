package com.pramod.dailyword.framework.prefmanagers

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoStartPrefManager @Inject constructor(@ApplicationContext context: Context) :
    BasePreferenceManager(
        PREFERENCES_NAME, context
    ) {

    fun clickedOnAlreadyEnabled() {
        editor.putBoolean(KEY_CLICK_ON_ALREADY_ENABLED, true).commit()
    }

    fun clickedOnSetting() {
        editor.putBoolean(KEY_CLICK_ON_SETTING, true).commit()
    }

    fun isAutoStartAlreadyEnabled() =
        sPrefManager.getBoolean(KEY_CLICK_ON_ALREADY_ENABLED, false)

    fun isClickedOnSetting() = sPrefManager.getBoolean(KEY_CLICK_ON_SETTING, false)

    companion object {
        private const val PREFERENCES_NAME = "auto_start_pref"
        private const val KEY_CLICK_ON_ALREADY_ENABLED = "click_on_already_enabled"
        private const val KEY_CLICK_ON_SETTING = "click_on_setting"

        fun newInstance(context: Context) = AutoStartPrefManager(context)

    }

}

