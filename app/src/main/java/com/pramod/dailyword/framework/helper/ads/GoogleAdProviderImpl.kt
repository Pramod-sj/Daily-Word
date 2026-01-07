package com.pramod.dailyword.framework.helper.ads

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.pramod.dailyword.R
import dagger.hilt.android.scopes.ActivityScoped
import timber.log.Timber
import javax.inject.Inject


@ActivityScoped
class GoogleAdProviderImpl @Inject constructor(
    private val activity: Activity,
) : AdProvider {

    private var interstitialAd: InterstitialAd? = null

    private var rewardedAd: RewardedAd? = null


    override fun loadNativeAd(
        context: Context,
        adUnitId: String,
        onLoaded: (AdViewWrapper) -> Unit,
        onFailed: (Throwable) -> Unit
    ) {
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { nativeAd ->

                var activityDestroyed = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    activityDestroyed = activity.isDestroyed
                }
                if (activityDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
                    nativeAd.destroy()
                    return@forNativeAd
                }

                val view = LayoutInflater.from(context)
                    .inflate(R.layout.ads_native_banner, null, false)

                onLoaded(
                    DefaultAdViewWrapperImpl(
                        nativeAd = nativeAd,
                        nativeAdView = populateNativeAdView(view, nativeAd)
                    )
                )
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Timber.e("Failed to load native ad: ${adError.message}")
                    onFailed(Throwable(adError.message))
                }
            })
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    override fun loadMediumSizedNativeAd(
        context: Context,
        adUnitId: String,
        onLoaded: (AdViewWrapper) -> Unit,
        onFailed: (Throwable) -> Unit
    ) {
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { nativeAd ->

                var activityDestroyed = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    activityDestroyed = activity.isDestroyed
                }
                if (activityDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
                    nativeAd.destroy()
                    return@forNativeAd
                }

                val view = LayoutInflater.from(context)
                    .inflate(R.layout.ads_native_medium, null, false)

                onLoaded(
                    DefaultAdViewWrapperImpl(
                        nativeAd = nativeAd,
                        nativeAdView = populateNativeAdView(view, nativeAd)
                    )
                )
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Timber.e("Failed to load native ad: ${adError.message}")
                    onFailed(Throwable(adError.message))
                }
            })
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    override fun loadInterstitial(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (Throwable) -> Unit
    ) {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            activity,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Timber.d("Interstitial ad loaded")
                    interstitialAd = ad
                    onLoaded()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Timber.e("Failed to load interstitial ad: ${adError.message}")
                    interstitialAd = null
                    onFailed(Throwable(adError.message))
                }
            }
        )
    }

    override fun showInterstitial(
        onAdDismissed: () -> Unit
    ) {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Timber.d("Interstitial ad was clicked")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                Timber.d("Interstitial ad dismissed")
                interstitialAd = null
                var activityDestroyed = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    activityDestroyed = activity.isDestroyed
                }
                if (activityDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
                    return
                }
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when ad fails to show.
                Timber.e("Interstitial ad failed to show: ${adError.message}")
                interstitialAd = null
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Timber.d("Interstitial ad recorded an impression")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Timber.d("Interstitial ad showed fullscreen content")
            }
        }
        interstitialAd?.show(activity) ?: run {
            Timber.e("Interstitial ad wasn't ready")
        }
    }

    override fun loadRewardedAd(
        adUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (Throwable) -> Unit
    ) {
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            activity,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {

                override fun onAdLoaded(ad: RewardedAd) {
                    Timber.d("Rewarded ad loaded")
                    rewardedAd = ad
                    onLoaded()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Timber.e("Failed to load rewarded ad: ${loadAdError.message}")
                    rewardedAd = null
                    onFailed(Throwable(loadAdError.message))
                }
            }
        )
    }

    override fun showRewardedAd(
        onUserEarnedReward: () -> Unit,
        onAdDismissed: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) {
            Timber.e("Rewarded ad not ready")
            return
        }

        var isRewarded = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdShowedFullScreenContent() {
                Timber.d("Rewarded ad showed fullscreen content")
            }

            override fun onAdDismissedFullScreenContent() {
                Timber.d("Rewarded ad dismissed")
                rewardedAd = null

                if (!activity.isFinishing && !activity.isDestroyed && isRewarded) {
                    onAdDismissed()
                }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Timber.e("Rewarded ad failed to show: ${adError.message}")
                rewardedAd = null
            }

            override fun onAdImpression() {
                Timber.d("Rewarded ad impression recorded")
            }

            override fun onAdClicked() {
                Timber.d("Rewarded ad clicked")
            }
        }

        ad.show(activity) { rewardItem: RewardItem ->
            isRewarded = true
            Timber.d(
                "User earned reward: ${rewardItem.amount} ${rewardItem.type}"
            )
            onUserEarnedReward()
        }
    }


    override fun destroy() {
        interstitialAd = null
    }


    private fun populateNativeAdView(view: View, nativeAd: NativeAd): NativeAdView {

        val nativeAdView = view.findViewById<NativeAdView>(R.id.native_ad_view)
        val adHeadline = view.findViewById<TextView>(R.id.ad_headline)
        val adBody = view.findViewById<TextView>(R.id.ad_body)
        val adCallToAction = view.findViewById<TextView>(R.id.ad_call_to_action)
        val nativeAdIcon = view.findViewById<ImageView>(R.id.native_ad_icon)
        val adRatingBar: RatingBar? = view.findViewById(R.id.ad_rating_bar)
        val mediaView: MediaView? = view.findViewById(R.id.ad_media)


        // Set the media view.
        if (mediaView != null) {
            nativeAdView.mediaView = mediaView
            mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
        }

        // Set other ad assets.
        nativeAdView.headlineView = adHeadline
        nativeAdView.bodyView = adBody
        nativeAdView.callToActionView = adCallToAction
        nativeAdView.iconView = nativeAdIcon
        //nativeAdView.priceView = unifiedAdBinding.adPrice
        nativeAdView.starRatingView = adRatingBar
        //nativeAdView.storeView = unifiedAdBinding.adStore

        // The headline and media content are guaranteed to be in every NativeAd.
        adHeadline.text = nativeAd.headline
        //nativeAd.mediaContent?.let { unifiedAdBinding.adMedia.setMediaContent(it) }

        // These assets aren't guaranteed to be in every NativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adBody.visibility = View.INVISIBLE
        } else {
            adBody.visibility = View.VISIBLE
            adBody.text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adCallToAction.visibility = View.INVISIBLE
        } else {
            adCallToAction.visibility = View.VISIBLE
            adCallToAction.text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            nativeAdIcon.visibility = View.GONE
        } else {
            nativeAdIcon.setImageDrawable(nativeAd.icon?.drawable)
            nativeAdIcon.visibility = View.VISIBLE
        }

        /*if (nativeAd.price == null) {
            unifiedAdBinding.adPrice.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adPrice.visibility = View.VISIBLE
            unifiedAdBinding.adPrice.text = nativeAd.price
        }*/

        /*if (nativeAd.store == null) {
            unifiedAdBinding.adStore.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adStore.visibility = View.VISIBLE
            unifiedAdBinding.adStore.text = nativeAd.store
        }*/

        if (nativeAd.starRating == null) {
            adRatingBar?.visibility = View.INVISIBLE
        } else {
            adRatingBar?.rating = nativeAd.starRating!!.toFloat()
            adRatingBar?.visibility = View.VISIBLE
        }


        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        nativeAdView.setNativeAd(nativeAd)

        val mediaContent = nativeAd.mediaContent
        // Updates the UI to say whether or not this ad has a video asset.
        if (mediaContent != null && mediaContent.hasVideoContent()) {
            val videoController = mediaContent.videoController
            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            videoController.videoLifecycleCallbacks =
                object : VideoController.VideoLifecycleCallbacks() {
                    override fun onVideoEnd() {
                        // Publishers should allow native ads to complete video playback before
                        // refreshing or replacing them with another ad in the same UI location.
                        super.onVideoEnd()
                    }
                }
        }

        return nativeAdView
    }
}
