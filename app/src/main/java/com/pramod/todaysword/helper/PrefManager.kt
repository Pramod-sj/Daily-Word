package com.pramod.todaysword.helper

import android.content.Context
import androidx.preference.PreferenceManager


class PrefManager(context: Context) {
    private val sPref = PreferenceManager.getDefaultSharedPreferences(context)
    private val editor = sPref.edit()

    companion object {
        private const val IS_NEW_USER = "IS_NEW_USER"
        private const val THEME_OPTION = "THEME_OPTION"
        private const val COLORED_NAV_BAR = "COLORED_NAV_BAR"
        private const val ENABLE_NOTIFICATION = "ENABLE_NOTIFICATION"
        private const val APP_LAUNCH_COUNT = "APP_LAUNCH_COUNT"

        @JvmStatic
        fun getInstance(context: Context): PrefManager = PrefManager(context)
    }

    fun isNewUser(): Boolean = sPref.getBoolean(IS_NEW_USER, true)

    fun setIsNewUser(isNewUser: Boolean) {
        editor.putBoolean(IS_NEW_USER, isNewUser).commit()
    }


    fun setThemeOption(option: ThemeManager.Options) {
        editor.putInt(THEME_OPTION, option.ordinal)
    }

    fun getThemeOption(): ThemeManager.Options =
        ThemeManager.Options.values()[sPref.getInt(
            THEME_OPTION,
            ThemeManager.Options.DEFAULT.ordinal
        )]

    fun isNotificationEnabled() = sPref.getBoolean(ENABLE_NOTIFICATION, true)

    fun setNotificationEnabled(enable: Boolean) {
        editor.putBoolean(ENABLE_NOTIFICATION, enable).commit()
    }

    fun isColoredNavBarEnabled() = sPref.getBoolean(COLORED_NAV_BAR, false)

    fun setColorNavBarEnabled(enable: Boolean) {
        editor.putBoolean(COLORED_NAV_BAR, enable).commit()
    }

    fun incrementAppLaunchCount() {
        val launchCount = sPref.getInt(APP_LAUNCH_COUNT, 0) + 1
        editor.putInt(APP_LAUNCH_COUNT, launchCount).commit()
    }

    fun getAppLaunchCount(): Int = sPref.getInt(APP_LAUNCH_COUNT, 1)

    //setting def value 1 because if appcount is not set this value must be greater than 0 else this will return true for 1st launch
    //for every 7 launch show rating dialog
    fun shouldShowRateNowDialog(): Boolean = getAppLaunchCount() % 7 == 0

}