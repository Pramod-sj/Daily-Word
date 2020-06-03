package com.pramod.todaysword.util

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.facebook.ads.*
import com.pramod.todaysword.R

class AdManager(private val context: Context) {
    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialShown = false
    fun loadInterstitialAd() {
        interstitialAd =
            InterstitialAd(context, context.resources.getString(R.string.interstitial_ad_id))
        interstitialAd!!.loadAd()
    }

    var FALLBACK_LOAD_COUNT = 0
    fun showInterStitialAd() {
        interstitialAd!!.setAdListener(object : AbstractAdListener() {
            override fun onError(ad: Ad, error: AdError) {
                super.onError(ad, error)
                if (FALLBACK_LOAD_COUNT < 3) {
                    interstitialAd!!.loadAd()
                    FALLBACK_LOAD_COUNT++
                }
            }

            override fun onAdLoaded(ad: Ad) {
                super.onAdLoaded(ad)
                if (interstitialAd!!.isAdLoaded && !isInterstitialShown) {
                    interstitialAd!!.show()
                    isInterstitialShown = true
                }
            }

            override fun onAdClicked(ad: Ad) {
                super.onAdClicked(ad)
            }

            override fun onInterstitialDisplayed(ad: Ad) {
                super.onInterstitialDisplayed(ad)
            }

            override fun onInterstitialDismissed(ad: Ad) {
                super.onInterstitialDismissed(ad)
                isInterstitialShown = false
            }
        })
        if (interstitialAd!!.isAdLoaded && !isInterstitialShown) {
            interstitialAd!!.show()
            isInterstitialShown = true
        }
    }

    var bannerAdView: AdView? = null
    var BANNER_FALLBACK_COUNT = 3
    fun showBanner(coordinatorLayout: CoordinatorLayout) {
        bannerAdView = AdView(
            context,
            context.resources.getString(R.string.banner_ad_id),
            AdSize.BANNER_HEIGHT_50
        )
        bannerAdView!!.loadAd()
        bannerAdView!!.setAdListener(object : AbstractAdListener() {
            override fun onError(ad: Ad, error: AdError) {
                super.onError(ad, error)
                if (BANNER_FALLBACK_COUNT > 0) {
                    bannerAdView!!.loadAd()
                    BANNER_FALLBACK_COUNT--
                }
            }
        })
        val params = CoordinatorLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        params.insetEdge = Gravity.BOTTOM
        bannerAdView!!.layoutParams = params
        coordinatorLayout.addView(bannerAdView)
    }

    fun destroyBannerAds() {
        if (bannerAdView != null) {
            bannerAdView!!.destroy()
        }
    }

    fun destroyInterstitialAds() {
        if (interstitialAd != null) {
            interstitialAd!!.destroy()
        }
    }

}