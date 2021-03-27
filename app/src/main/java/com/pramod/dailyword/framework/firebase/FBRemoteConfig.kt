package com.pramod.dailyword.framework.firebase

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.R

class FBRemoteConfig {
    private val remoteConfig = Firebase.remoteConfig.apply {
        setConfigSettingsAsync(remoteConfigSettings {
            //360 i.e. 1 hour
            minimumFetchIntervalInSeconds = 3600
        })
        setDefaultsAsync(R.xml.remote_config_defaults)
    }

    companion object {
        const val TAG = "FBRemoteConfig"

        const val REMOTE_CONFIG_KEY_BASE_URL = "base_url"

        const val REMOTE_CONFIG_KEY_ADS = "ads"

        const val REMOTE_CONFIG_KEY_THANK_YOU_LOTTIE_URL = "lottie_donate_page_thank_you_url"

        @JvmStatic
        fun newInstance(): FBRemoteConfig = FBRemoteConfig()
    }

    init {
        remoteConfig.fetch(0).addOnCompleteListener {
            if (it.isSuccessful) {
                remoteConfig.activate()
                Log.i(TAG, "Remote configs are fetched")
            } else {
                Log.i(TAG, "Remote configs are not fetch : Error ${it.exception.toString()}")
            }
        }
    }


    fun isAdsEnabled(): Boolean = /*remoteConfig.getBoolean(REMOTE_CONFIG_KEY_ADS)*/false

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


}