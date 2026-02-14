package com.pramod.dailyword.framework.helper.ads

import android.util.Log
import android.view.View
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

class DefaultAdViewWrapperImpl(
    private val nativeAd: NativeAd,
    private val nativeAdView: NativeAdView
) : AdViewWrapper {

    companion object {
        private const val TAG = "DefaultAdViewWrapperImp"
    }

    override fun getView(): View {
        return nativeAdView
    }

    override fun destroy() {
        Log.i(TAG, "destroy: ad")
        nativeAd.destroy()
        nativeAdView.removeAllViews()
        nativeAdView.destroy()
    }

}