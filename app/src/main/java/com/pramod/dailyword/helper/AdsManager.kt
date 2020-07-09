package com.pramod.dailyword.helper

import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import com.facebook.ads.*
import com.pramod.dailyword.databinding.BannerNativeAdBinding
import com.pramod.dailyword.firebase.FBRemoteConfig
import com.pramod.dailyword.R
import kotlinx.android.synthetic.main.banner_native_ad.view.*
import java.util.ArrayList

class AdsManager private constructor(private val context: Context) {
    private val fbRemoteConfig = FBRemoteConfig()

    private var bannerAdView: AdView? = null
    private var interstitialAdView: InterstitialAd? = null
    private var isInterstitialAdShown = false

    companion object {
        const val NATIVE_AD_ID = "2986832738063599_3129116340501904"
        const val NATIVE_BANNER_ID_1 = "2986832738063599_3127533043993567"
        const val NATIVE_BANNER_ID_2 = "2986832738063599_3129110357169169"
        const val NATIVE_BANNER_ID_3 = "2986832738063599_3129112467168958"
        const val NATIVE_INTERSTITIAL_AD = "2986832738063599_2986834191396787"
        const val TAG = "AdsManager"
        fun newInstance(context: Context): AdsManager {
            return AdsManager(context)
        }
    }

    fun showInterstitialAd(): Boolean {
        if (!fbRemoteConfig.isAdsEnabled()) {
            Log.i(TAG, "Ads are disabled")
            return false
        }
        interstitialAdView = InterstitialAd(context, null).also {
            it.loadAd()
            it.setAdListener(object : AbstractAdListener() {
                override fun onAdLoaded(ad: Ad?) {
                    super.onAdLoaded(ad)
                    Log.i(TAG, "Interstitial Ad Load")
                    if (it.isAdLoaded && !isInterstitialAdShown) {
                        it.show()
                        isInterstitialAdShown = true
                    }
                }

                override fun onInterstitialDisplayed(ad: Ad?) {
                    super.onInterstitialDisplayed(ad)
                    Log.i(TAG, "Interstitial Ad displayed")
                }

                override fun onInterstitialDismissed(ad: Ad?) {
                    super.onInterstitialDismissed(ad)
                    Log.i(TAG, "Interstitial Ad dismissed")
                    isInterstitialAdShown = false
                }

                override fun onError(ad: Ad?, error: AdError?) {
                    super.onError(ad, error)
                    Log.i(TAG, "Interstitial Ad Error: ${error.toString()}")
                }
            })
        }
        return true
    }


    /**
     * Current it can place banner ads at bottom of coordinator layout
     */
    fun showBannerAdInCoordinateLayout(layout: CoordinatorLayout): Boolean {
        if (!fbRemoteConfig.isAdsEnabled()) {
            Log.i(TAG, "Ads are disabled")
            return false
        }
        bannerAdView = AdView(context, null, AdSize.BANNER_HEIGHT_50)
            .also {
                it.loadAd()
                it.setAdListener(object : AdListener {
                    override fun onAdClicked(p0: Ad?) {
                        Log.i(TAG, "Banner Ad clicked")
                    }

                    override fun onError(p0: Ad?, p1: AdError?) {
                        Log.i(TAG, "Banner Ad ${p1.toString()}")
                    }

                    override fun onAdLoaded(p0: Ad?) {
                        Log.i(TAG, "Banner Ad loaded")
                    }

                    override fun onLoggingImpression(p0: Ad?) {
                        Log.i(TAG, "Banner Ad logging impression")
                    }

                })
            }
        val layoutParams: CoordinatorLayout.LayoutParams =
            layout.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.gravity = Gravity.BOTTOM + Gravity.CENTER_HORIZONTAL
        bannerAdView!!.layoutParams = layoutParams
        layout.addView(bannerAdView)
        return true
    }


    fun showNativeBannerAd(adId: String, nativeAdLayout: NativeAdLayout): Boolean {
        if (!fbRemoteConfig.isAdsEnabled()) {
            Log.i(TAG, "Ads are disabled")
            return false
        }

        NativeBannerAd(context, adId).also {
            it.loadAd()
            it.setAdListener(object : NativeAdListener {
                override fun onAdClicked(p0: Ad?) {

                    Log.i(TAG, "Native ad clicked")
                }

                override fun onMediaDownloaded(p0: Ad?) {
                    Log.i(TAG, "Native ad media downlaoded")

                }

                override fun onError(p0: Ad?, p1: AdError?) {
                    Log.i(TAG, "Native ad Error: ${p1.toString()}")
                }

                override fun onAdLoaded(p0: Ad?) {
                    Log.i(TAG, "Native ad loaded")
                    if (p0 == null) {
                        Log.i(TAG, "Native ad loaded is null")
                        return
                    }
                    inflateNativeView(nativeAdLayout, it)

                }

                override fun onLoggingImpression(p0: Ad?) {
                    Log.i(TAG, "Native ad logging impression")

                }

            })
        }

        return true;
    }


    private fun inflateNativeView(nativeAdLayout: NativeAdLayout, nativeBannerAd: NativeBannerAd) {

        nativeBannerAd.unregisterView()
        val binding: BannerNativeAdBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.banner_native_ad,
                nativeAdLayout,
                false
            )
        nativeAdLayout.addView(binding.root)

        //bind adchoice icon
        val adOptionsView: AdOptionsView =
            AdOptionsView(nativeAdLayout.context, nativeBannerAd, nativeAdLayout)
        binding.adChoiceContainer.removeAllViews()
        binding.adChoiceContainer.addView(adOptionsView, 0)

        //bind other ad info
        binding.nativeAdTitle.text = nativeBannerAd.advertiserName
        binding.nativeAdSocialContext.text = nativeBannerAd.adSocialContext
        binding.nativeAdSponseredLabel.text = nativeBannerAd.sponsoredTranslation
        binding.nativeAdButton.text = nativeBannerAd.adCallToAction
        binding.nativeAdButton.isVisible = nativeBannerAd.hasCallToAction()

        //registering click callbacks

        val clickableViews = ArrayList<View>()
        clickableViews.add(binding.nativeAdTitle)
        clickableViews.add(binding.nativeAdButton)
        nativeBannerAd.registerViewForInteraction(
            binding.root,
            binding.nativeAdIcon,
            clickableViews
        )

    }


    fun destroyAdsIfActive() {
        interstitialAdView?.destroy()
        bannerAdView?.destroy()
    }
}

class AdsBindingAdapter {

    companion object {

        @JvmStatic
        @BindingAdapter(value = ["showNativeAd", "adId", "showAdWithSomeDelay"])
        fun showNativeAd(
            nativeAdLayout: NativeAdLayout,
            showNativeAd: Boolean,
            adId: String,
            showAdWithSomeDelay: Long
        ) {
            nativeAdLayout.isVisible = showNativeAd
            if (showNativeAd) {
                Handler().postDelayed({
                    val adsManager = AdsManager.newInstance(nativeAdLayout.context)
                    adsManager.showNativeBannerAd(adId, nativeAdLayout)
                }, showAdWithSomeDelay)
            }
        }


    }

}