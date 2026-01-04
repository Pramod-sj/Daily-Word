package com.pramod.dailyword.framework.helper.ads

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.pramod.dailyword.databinding.AdsNativeBannerBinding
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

                onLoaded(
                    DefaultAdViewWrapperImpl(
                        nativeAd = nativeAd,
                        nativeAdView = populateNativeAdView(context, nativeAd)
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


    private fun populateNativeAdView(context: Context, nativeAd: NativeAd): NativeAdView {

        val unifiedAdBinding: AdsNativeBannerBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(context),
                com.pramod.dailyword.R.layout.ads_native_banner,
                null,
                false
            )

        val nativeAdView = unifiedAdBinding.nativeAdView

        // Set the media view.
        //nativeAdView.mediaView = unifiedAdBinding.adMedia

        // Set other ad assets.
        nativeAdView.headlineView = unifiedAdBinding.adHeadline
        nativeAdView.bodyView = unifiedAdBinding.adBody
        nativeAdView.callToActionView = unifiedAdBinding.adCallToAction
        nativeAdView.iconView = unifiedAdBinding.nativeAdIcon
        //nativeAdView.priceView = unifiedAdBinding.adPrice
        //nativeAdView.starRatingView = unifiedAdBinding.adStars
        //nativeAdView.storeView = unifiedAdBinding.adStore
        //nativeAdView.advertiserView = unifiedAdBinding.adAdvertiser

        // The headline and media content are guaranteed to be in every NativeAd.
        unifiedAdBinding.adHeadline.text = nativeAd.headline
        //nativeAd.mediaContent?.let { unifiedAdBinding.adMedia.setMediaContent(it) }

        // These assets aren't guaranteed to be in every NativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            unifiedAdBinding.adBody.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adBody.visibility = View.VISIBLE
            unifiedAdBinding.adBody.text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            unifiedAdBinding.adCallToAction.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adCallToAction.visibility = View.VISIBLE
            unifiedAdBinding.adCallToAction.text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            unifiedAdBinding.nativeAdIcon.visibility = View.GONE
        } else {
            unifiedAdBinding.nativeAdIcon.setImageDrawable(nativeAd.icon?.drawable)
            unifiedAdBinding.nativeAdIcon.visibility = View.VISIBLE
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

        /*if (nativeAd.starRating == null) {
            unifiedAdBinding.adStars.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adStars.rating = nativeAd.starRating!!.toFloat()
            unifiedAdBinding.adStars.visibility = View.VISIBLE
        }*/

        /*if (nativeAd.advertiser == null) {
            unifiedAdBinding.adAdvertiser.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adAdvertiser.text = nativeAd.advertiser
            unifiedAdBinding.adAdvertiser.visibility = View.VISIBLE
        }*/

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        nativeAdView.setNativeAd(nativeAd)

        val mediaContent = nativeAd.mediaContent
        /*
                // Updates the UI to say whether or not this ad has a video asset.
                if (mediaContent != null && mediaContent.hasVideoContent()) {
                    val videoController = mediaContent.videoController
                    mainActivityBinding.videostatusText.text =
                        String.format(
                            Locale.getDefault(),
                            "Video status: Ad contains a %.2f:1 video asset.",
                            mediaContent.aspectRatio,
                        )
                    // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
                    // VideoController will call methods on this object when events occur in the video
                    // lifecycle.
                    videoController.videoLifecycleCallbacks =
                        object : VideoController.VideoLifecycleCallbacks() {
                            override fun onVideoEnd() {
                                // Publishers should allow native ads to complete video playback before
                                // refreshing or replacing them with another ad in the same UI location.
                                mainActivityBinding.refreshButton.isEnabled = true
                                mainActivityBinding.videostatusText.text =
                                    "Video status: Video playback has ended."
                                super.onVideoEnd()
                            }
                        }
                } else {
                    mainActivityBinding.videostatusText.text =
                        "Video status: Ad does not contain a video asset."
                    mainActivityBinding.refreshButton.isEnabled = true
                }*/

        return unifiedAdBinding.nativeAdView
    }
}
