package com.pramod.dailyword.framework.helper.ads

import android.content.Context

interface AdProvider {

    fun loadNativeAd(
        context: Context,
        adUnitId: String,
        onLoaded: (AdViewWrapper) -> Unit,
        onFailed: (Throwable) -> Unit
    )

    fun loadInterstitial(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (Throwable) -> Unit
    )

    fun showInterstitial(onAdDismissed: () -> Unit)

    fun destroy()

}
