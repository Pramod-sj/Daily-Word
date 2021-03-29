package com.pramod.dailyword.framework.prefmanagers

import android.content.Context
import androidx.lifecycle.LiveData
import com.pramod.dailyword.BuildConfig


class PrefManager(context: Context) :
    BasePreferenceManager(null, context),
    AppLaunchCountContracts,
    MWCreditDialogContracts,
    IsNewUserContracts,
    LastSavedAppVersionContracts,
    ShowBadgeContracts {


    override fun shouldShowMWCreditDialog(): Boolean {
        return sPrefManager.getBoolean(SHOW_INITIAL_CREDIT_DIALOG, true)
    }

    override fun changeMWCreditDialogShown(shown: Boolean) {
        editor.putBoolean(SHOW_INITIAL_CREDIT_DIALOG, shown).apply()
    }


    override fun isNewUser(): Boolean {
        return sPrefManager.getBoolean(IS_NEW_USER, true)
    }

    override fun markUserAsOld() {
        editor.putBoolean(IS_NEW_USER, false).apply()
    }


    override fun incrementAppLaunchCount() {
        val launchCount = sPrefManager.getInt(APP_LAUNCH_COUNT, 0) + 1
        editor.putInt(APP_LAUNCH_COUNT, launchCount).apply()
    }

    override fun getAppLaunchCount(): Int = sPrefManager.getInt(APP_LAUNCH_COUNT, 1)

    /**
     * setting def value 1 because if app count return 0
     * then for very first launch it will return true
     * for every 30 launch show rating dialog
     */
    fun shouldShowRateNowDialog(): Boolean {
        return getAppLaunchCount() % 30 == 0
    }

    override fun getLastSavedAppVersion(): Int {
        return sPrefManager.getInt(KEY_APP_VERSION, 1)
    }

    override fun updateLastSavedAppVersion() {
        editor.putInt(KEY_APP_VERSION, BuildConfig.VERSION_CODE).apply()
    }

    override fun setShowBadge(show: Boolean) {
        editor.putBoolean(KEY_SHOW_BADGE, show).apply()
    }

    override fun getShowBadge(): Boolean {
        return sPrefManager.getBoolean(KEY_SHOW_BADGE, true)
    }

    override fun getShowBadgeLiveData(): LiveData<Boolean> {
        return SPrefBooleanLiveData(sPrefManager, KEY_SHOW_BADGE, true)
    }

    companion object {
        private const val IS_NEW_USER = "IS_NEW_USER"
        private const val APP_LAUNCH_COUNT = "APP_LAUNCH_COUNT"
        private const val SHOW_INITIAL_CREDIT_DIALOG = "show_initial_credit_dialog"
        private const val KEY_APP_VERSION = "app_version"
        private const val KEY_SHOW_BADGE = "show_badge"


        @JvmStatic
        fun getInstance(context: Context): PrefManager = PrefManager(context)
    }


}

interface AppLaunchCountContracts {
    fun incrementAppLaunchCount()

    fun getAppLaunchCount(): Int
}

interface IsNewUserContracts {
    fun isNewUser(): Boolean

    fun markUserAsOld()
}

interface MWCreditDialogContracts {
    fun shouldShowMWCreditDialog(): Boolean

    fun changeMWCreditDialogShown(shown: Boolean)
}


/**
 *This interface contains two method one to get app version stored in pref
 * and other is to update that app version
 * this method are used to evaluate whether to show changelog
 */
interface LastSavedAppVersionContracts {

    fun getLastSavedAppVersion(): Int

    fun updateLastSavedAppVersion()
}

interface ShowBadgeContracts {
    fun setShowBadge(show: Boolean)

    fun getShowBadge(): Boolean

    fun getShowBadgeLiveData(): LiveData<Boolean>
}
