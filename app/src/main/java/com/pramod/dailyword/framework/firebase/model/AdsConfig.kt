package com.pramod.dailyword.framework.firebase.model

import androidx.annotation.Keep

@Keep
data class AdsConfig(
    val adsEnabled: Boolean?,

    val adsEnabledCountries: List<String>?,
    val adUnits: Map<String, AdUnitConfig>?,

    val actionCountForInterstitial: Int?,
    val maxInterstitialsPerSession: Int?,

    val disableAdForPremiumUser: Boolean?,

    val disabledAdsDays: Int?,

    val showRemoveAdOptionInMenu: Boolean?,
)


@Keep
data class AdUnitConfig(
    val adUnitId: String?,
    val enabled: Boolean?,
    val showPostInterstitialDialog: Boolean?
)