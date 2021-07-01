package com.pramod.dailyword.framework.firebase

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FBRemoteConfig @Inject constructor(
    private val prefManager: PrefManager
) {

    private val remoteConfig = Firebase.remoteConfig.apply {
        setConfigSettingsAsync(remoteConfigSettings {
            //360 i.e. 1 hour
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
        })
        setDefaultsAsync(default_configs)
    }

    companion object {
        const val TAG = "FBRemoteConfig"

        private const val REMOTE_CONFIG_KEY_BASE_URL = "base_url"

        /**
         * not using anymore after v2.0.0
         */
        private const val REMOTE_CONFIG_KEY_ADS = "ads"

        /**
         * remote key to store ad value specific to countries
         */
        private const val REMOTE_CONFIG_KEY_ADS_ENABLED = "ads_enabled"

        const val REMOTE_CONFIG_KEY_THANK_YOU_LOTTIE_URL = "lottie_donate_page_thank_you_url"
        const val REMOTE_CONFIG_KEY_DONATE_PAGE_LOTTIE_URL = "donate_page_lottie_url"

        const val REMOTE_CONFIG_KET_LATEST_RELEASE_NOTE = "latest_release_json"

        private val default_configs = mapOf(
            REMOTE_CONFIG_KEY_BASE_URL to BuildConfig.API_BASE_URL,
            REMOTE_CONFIG_KEY_ADS_ENABLED to "{\"all\":false,\"in\":false,\"us\":false,\"gb\":false,\"others\":false}"
        )
    }

    init {
        remoteConfig.fetchAndActivate().addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "Remote configs are fetched and activated")
            } else {
                Log.i(TAG, "Remote configs are not fetch : Error ${it.exception.toString()}")
            }
        }
    }


    fun isAdsEnabled(): Boolean {
        val adString = remoteConfig.getString(REMOTE_CONFIG_KEY_ADS_ENABLED)
        try {
            val adsEnabled: AdsEnabled =
                Gson().fromJson(adString, AdsEnabled::class.java)
            return when {
                adsEnabled.enabled_ad_all -> {
                    //if this is true then show ads to all the users irrespective of countries
                    true
                }
                adsEnabled.enable_ad_in -> {
                    //check if current user is IN
                    prefManager.getCountryCode() == SupportedFBTopicCounties.IN.name
                }
                adsEnabled.enable_ad_us -> {
                    //check if current user is US
                    prefManager.getCountryCode() == SupportedFBTopicCounties.US.name
                }
                adsEnabled.enable_ad_gb -> {
                    //check if current user is GB (UK)
                    prefManager.getCountryCode() == SupportedFBTopicCounties.GB.name
                }
                adsEnabled.enabled_ad_other -> {
                    //check if current user is of other country
                    prefManager.getCountryCode() == SupportedFBTopicCounties.OTHERS.name
                }
                else -> {
                    //if no condition matched return false
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun baseUrl(): String {
        val baseUrl = remoteConfig.getString(REMOTE_CONFIG_KEY_BASE_URL)
        if (baseUrl.isEmpty() || baseUrl.isBlank()) {
            return BuildConfig.API_BASE_URL
        }
        return baseUrl
    }

    fun getThankYouLottieFileUrl(): String {
        val url = remoteConfig.getString(REMOTE_CONFIG_KEY_THANK_YOU_LOTTIE_URL)
        if (url.isEmpty() || url.isBlank()) {
            return BuildConfig.URL_LOTTIE_THANK_YOU
        }
        return url
    }


    fun getDonatePageLottieFileUrl(): String {
        val url = remoteConfig.getString(REMOTE_CONFIG_KEY_DONATE_PAGE_LOTTIE_URL)
        if (url.isEmpty() || url.isBlank()) {
            return BuildConfig.URL_LOTTIE_DONATE_PAGE
        }
        return url
    }

    fun getLatestReleaseNote(): ReleaseNote? {
        return Gson().fromJson(
            remoteConfig.getString(REMOTE_CONFIG_KET_LATEST_RELEASE_NOTE),
            ReleaseNote::class.java
        )?.let { releaseNote ->
            if (releaseNote.versionCode > BuildConfig.VERSION_CODE) {
                releaseNote
            } else null
        }
    }



    data class ReleaseNote(
        @SerializedName("version_code")
        val versionCode: Long,
        @SerializedName("version_name")
        val versionName: String,
        @SerializedName("changes")
        val changes: List<String>,
        @SerializedName("is_force_update")
        val isForceUpdate: Boolean
    )


    data class AdsEnabled(
        @SerializedName("all")
        val enabled_ad_all: Boolean,
        @SerializedName("in")
        val enable_ad_in: Boolean,
        @SerializedName("us")
        val enable_ad_us: Boolean,
        @SerializedName("gb")
        val enable_ad_gb: Boolean,
        @SerializedName("other")
        val enabled_ad_other: Boolean
    )
}