package com.pramod.dailyword.framework.firebase.model

data class AdsConfig(
    val adsEnabled: Boolean,

    val adsEnabledCountries: List<String>,
    val adsEnabledScreen: Map<String, AdTypeEnableStatus>,

    val actionCountForInterstitial: Int,
    val maxInterstitialsPerSession: Int,

    val disableAdForPremiumUser: Boolean,
)

data class AdTypeEnableStatus(
    val banner: Boolean,
    val interstitial: Boolean
)