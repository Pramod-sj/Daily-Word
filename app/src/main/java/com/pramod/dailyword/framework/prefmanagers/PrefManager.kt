package com.pramod.dailyword.framework.prefmanagers

import android.content.Context
import androidx.lifecycle.LiveData
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.framework.firebase.SupportedFBTopicCounties
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefManager @Inject constructor(@ApplicationContext context: Context) :
    BasePreferenceManager(null, context),
    AppLaunchCountContracts,
    MWCreditDialogContracts,
    IsNewUserContracts,
    LastSavedAppVersionContracts,
    HideBadgeContracts,
    CountryCodeContracts,
    DonatedContract,
    SupportUsDialogContract {


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
        val launchCount = getAppLaunchCount() + 1
        editor.putInt(APP_LAUNCH_COUNT, launchCount).apply()
    }

    override fun getAppLaunchCount(): Int = sPrefManager.getInt(APP_LAUNCH_COUNT, 0)

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

    override fun setHideBadge(hide: Boolean) {
        editor.putBoolean(KEY_SHOW_BADGE, hide).apply()
    }

    override fun getHideBadge(): Boolean {
        return sPrefManager.getBoolean(KEY_SHOW_BADGE, false)
    }

    override fun toggleHideBadge() {
        editor.putBoolean(KEY_SHOW_BADGE, !getHideBadge()).apply()
    }

    override fun getHideBadgeLiveData(): LiveData<Boolean> {
        return SPrefBooleanLiveData(sPrefManager, KEY_SHOW_BADGE, false)
    }

    companion object {
        private const val IS_NEW_USER = "IS_NEW_USER"
        private const val APP_LAUNCH_COUNT = "APP_LAUNCH_COUNT"
        private const val SHOW_INITIAL_CREDIT_DIALOG = "show_initial_credit_dialog"
        private const val KEY_APP_VERSION = "app_version"
        private const val KEY_SHOW_BADGE = "show_badge"

        private const val KEY_IS_DONATED = "is_donated"

        //how many time support us dialog method was called
        private const val KEY_SUPPORT_US_CALLED_COUNT = "support_us_called_count"
        private const val KEY_SHOW_SUPPORT_US_NEVER = "show_support_us_never"

        /**
         * country code
         */
        private const val KEY_USER_COUNTRY_CODE = "user_country_code"


        @JvmStatic
        fun getInstance(context: Context): PrefManager = PrefManager(context)
    }

    override fun setCountryCode(code: String) {
        sPrefManager.edit().putString(KEY_USER_COUNTRY_CODE, code).apply()
    }

    override fun getCountryCode(): String {
        return sPrefManager.getString(KEY_USER_COUNTRY_CODE, SupportedFBTopicCounties.OTHERS.name)
            ?: SupportedFBTopicCounties.OTHERS.name
    }

    override fun setHasDonated(donated: Boolean) {
        sPrefManager.edit().putBoolean(KEY_IS_DONATED, donated).apply()
    }

    override fun hasDonated(): Boolean? {
        return if (sPrefManager.contains(KEY_IS_DONATED)) sPrefManager.getBoolean(
            KEY_IS_DONATED,
            false
        ) else null
    }

    override fun incrementSupportUsDialogCalledCount() {
        sPrefManager.edit().putInt(KEY_SUPPORT_US_CALLED_COUNT, getSupportUsDialogCalledCount() + 1)
            .apply()
    }

    override fun getSupportUsDialogCalledCount(): Int {
        return sPrefManager.getInt(KEY_SUPPORT_US_CALLED_COUNT, 0)
    }

    override fun setNeverShowSupportUsDialog(neverShow: Boolean) {
        sPrefManager.edit().putBoolean(KEY_SHOW_SUPPORT_US_NEVER, neverShow).apply()
    }

    override fun getNeverShowSupportUsDialog(): Boolean {
        return sPrefManager.getBoolean(KEY_SHOW_SUPPORT_US_NEVER,false)
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

interface HideBadgeContracts {
    fun setHideBadge(hide: Boolean)

    fun getHideBadge(): Boolean

    fun toggleHideBadge()

    fun getHideBadgeLiveData(): LiveData<Boolean>
}

interface CountryCodeContracts {

    fun setCountryCode(code: String)

    /**
     * Default value is OTHERS
     */
    fun getCountryCode(): String
}

interface DonatedContract {
    fun setHasDonated(donated: Boolean)

    fun hasDonated(): Boolean?
}

interface SupportUsDialogContract {
    fun incrementSupportUsDialogCalledCount()

    fun getSupportUsDialogCalledCount(): Int

    fun setNeverShowSupportUsDialog(neverShow: Boolean)

    fun getNeverShowSupportUsDialog(): Boolean

}
