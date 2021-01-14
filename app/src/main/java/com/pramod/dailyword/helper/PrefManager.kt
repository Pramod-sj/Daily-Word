package com.pramod.dailyword.helper

import android.content.Context
import android.os.Build
import androidx.preference.PreferenceManager
import com.pramod.dailyword.BuildConfig


class PrefManager(context: Context) {
    private val sPref = PreferenceManager.getDefaultSharedPreferences(context)
    private val editor = sPref.edit()

    companion object {
        private const val IS_NEW_USER = "IS_NEW_USER"
        private const val APP_LAUNCH_COUNT = "APP_LAUNCH_COUNT"
        private const val NEVER_SHOW_RATING = "NEVER_SHOW_RATING"
        private const val SHOW_INITIAL_CREDIT_DIALOG = "show_initial_credit_dialog"
        private const val KEY_APP_VERSION = "app_version"

        @JvmStatic
        fun getInstance(context: Context): PrefManager = PrefManager(context)
    }

    fun changeShowInitialCreditDialogStatus(status: Boolean) {
        editor.putBoolean(SHOW_INITIAL_CREDIT_DIALOG, status).commit()
    }

    fun getShowInitailCreditDialogStatus() = sPref.getBoolean(SHOW_INITIAL_CREDIT_DIALOG, true)

    fun isNewUser(): Boolean = sPref.getBoolean(IS_NEW_USER, true)

    fun setIsNewUser(isNewUser: Boolean) {
        editor.putBoolean(IS_NEW_USER, isNewUser).commit()
    }

    fun incrementAppLaunchCount() {
        val launchCount = sPref.getInt(APP_LAUNCH_COUNT, 0) + 1
        editor.putInt(APP_LAUNCH_COUNT, launchCount).commit()
    }

    fun getAppLaunchCount(): Int = sPref.getInt(APP_LAUNCH_COUNT, 1)

    //setting def value 1 because if appcount is not set this value must be greater than 0 else this will return true for 1st launch
    //for every 30 launch show rating dialog
    fun shouldShowRateNowDialog(): Boolean =
        getAppLaunchCount() % 30 == 0 && isUserEverClickedOnNeverOrRateNow()


    fun setUserClickedNever() {
        editor.putBoolean(NEVER_SHOW_RATING, true).commit()
    }

    fun setUserClickedRateNow() {
        editor.putBoolean(NEVER_SHOW_RATING, true).commit()
    }

    private fun isUserEverClickedOnNeverOrRateNow() = !sPref.getBoolean(NEVER_SHOW_RATING, false)

    //app version
    fun updateAppVersion() = editor.putInt(KEY_APP_VERSION, BuildConfig.VERSION_CODE).commit()

    fun getLastAppVersion(): Int = sPref.getInt(KEY_APP_VERSION, 1)

}