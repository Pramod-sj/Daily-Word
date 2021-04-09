package com.pramod.dailyword.framework.firebase

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.framework.prefmanagers.PrefManager

class FBRemoteConfig(
    private val prefManager: PrefManager
) {

    private val firebaseRemoteConfig = Firebase.remoteConfig.apply {
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

        private val default_configs = mapOf(
            REMOTE_CONFIG_KEY_BASE_URL to BuildConfig.API_BASE_URL,
            REMOTE_CONFIG_KEY_ADS_ENABLED to "{\"all\":false,\"in\":false,\"us\":false,\"gb\":false,\"others\":false}"
        )
    }

    init {
        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "Remote configs are fetched and activated")
            } else {
                Log.i(TAG, "Remote configs are not fetch : Error ${it.exception.toString()}")
            }
        }
    }


    fun isAdsEnabled(): Boolean {
        val adString = firebaseRemoteConfig.getString(REMOTE_CONFIG_KEY_ADS_ENABLED)
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
        val baseUrl = firebaseRemoteConfig.getString(REMOTE_CONFIG_KEY_BASE_URL)
        if (baseUrl.isEmpty() || baseUrl.isBlank()) {
            return BuildConfig.API_BASE_URL
        }
        return baseUrl
    }

    fun getThankYouLottieFileUrl(): String {
        val url = firebaseRemoteConfig.getString(REMOTE_CONFIG_KEY_THANK_YOU_LOTTIE_URL)
        if (url.isEmpty() || url.isBlank()) {
            return BuildConfig.URL_LOTTIE_THANK_YOU
        }
        return url
    }


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