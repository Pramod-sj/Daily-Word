package com.pramod.dailyword.framework.helper.ads

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.pramod.dailyword.databinding.AdsNativeBannerBinding
import dagger.hilt.android.scopes.ActivityScoped
import timber.log.Timber
import javax.inject.Inject

@ActivityScoped
class GoogleAdProviderImpl @Inject constructor(
    private val activity: Activity
) : AdProvider {

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
        TODO("Not yet implemented")
    }

    override fun showInterstitial() {
        TODO("Not yet implemented")
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
