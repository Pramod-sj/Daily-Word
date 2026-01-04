package com.pramod.dailyword.framework.helper.ads

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.compose.ui.platform.ComposeView
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import dagger.hilt.android.scopes.ActivityScoped
import timber.log.Timber
import java.util.LinkedList
import javax.inject.Inject

@ActivityScoped
class AdController @Inject constructor(
    private val adProvider: AdProvider,
    private val fbRemoteConfig: FBRemoteConfig,
    private val prefManager: PrefManager,
    private val activity: Activity,
    private val interstitialAdTracker: InterstitialAdTracker
) {

    private val screenName = activity.localClassName.split(".").lastOrNull()

    private val countryCode = prefManager.getCountryCode()

    private val adConfig = fbRemoteConfig.getAdsConfig()

    val isAdEnabled: Boolean by lazy {
        adConfig.adsEnabled //&& !adConfig.disableAdForPremiumUser
    }

    val isBannerAdEnabled: Boolean by lazy {
        isAdEnabled && adConfig.adsEnabledCountries.contains(countryCode)
            && adConfig.adsEnabledScreen[screenName]?.banner ?: false
    }

    val isInterstitialAdEnabled: Boolean by lazy {
        isAdEnabled && adConfig.adsEnabledCountries.contains(countryCode)
            && adConfig.adsEnabledScreen[screenName]?.interstitial ?: false
    }

    // Cache for loaded native ad views. Keyed by the container they are in.
    private val activeAdViews = mutableMapOf<ViewGroup, AdViewWrapper>()

    // Cache for shimmer placeholder views. A simple queue is effective for RecyclerView.
    private val shimmerViewCache = LinkedList<ComposeView>()

    private var isInterstitialLoaded = false

    private fun getShimmerView(context: Context): ComposeView {
        // Reuse a cached shimmer view if available, otherwise create a new one.
        return shimmerViewCache.poll() ?: createShimmerView(context)
    }

    private fun createShimmerView(context: Context): ComposeView {
        return ComposeView(context).apply {
            setContent {
                // Your shimmer composable
                NativeBannerPlaceholderShimmer()
            }
        }
    }

    private fun releaseShimmerView(view: ComposeView) {
        // Add the unused shimmer view back to the cache for later reuse.
        (view.parent as? ViewGroup)?.removeView(view)
        shimmerViewCache.offer(view)
    }

    fun loadBanner(
        container: ViewGroup,
        adUnitId: String = "ca-app-pub-3940256099942544/2247696110" // Test Ad Unit
    ) {

        if (!isBannerAdEnabled) {
            Timber.i("Banner ad is not enabled for screen: $screenName; country: $countryCode")
            return
        }

        activeAdViews[container]?.destroy()

        // Get a shimmer view (either from cache or new) and show it.
        val shimmer = getShimmerView(container.context)
        container.removeAllViews()
        container.addView(shimmer)

        adProvider.loadNativeAd(
            context = container.context,
            adUnitId = adUnitId,
            onLoaded = { wrapper ->
                container.removeAllViews()
                container.addView(wrapper.getView())
                activeAdViews[container] = wrapper
            },
            onFailed = {
                // Ad failed to load.
                // You can either leave the shimmer or hide the container.
                // For this example, we'll just leave the shimmer.
                // Make sure to release the shimmer if you hide the view.
                container.removeAllViews()
                CollapseHeightAnimation(container).apply {
                    duration = 100L
                    setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) = Unit

                        override fun onAnimationEnd(animation: Animation?) = Unit

                        override fun onAnimationRepeat(animation: Animation?) = Unit
                    })
                    container.startAnimation(this)
                }
            }
        )
    }

    fun hideBanner(container: ViewGroup) {
        if (!isBannerAdEnabled) {
            Timber.i("Banner ad is not enabled for screen: $screenName; country: $countryCode")
            return
        }
        // Called when the view is recycled or hidden.
        activeAdViews.remove(container)?.destroy()
        (container.getChildAt(0) as? ComposeView)?.let {
            releaseShimmerView(it)
        }
        container.removeAllViews()
    }


    fun loadInterstitialAd() {

        if (isAdEnabled) {
            interstitialAdTracker.incrementActionCount()
        }

        if (!isInterstitialAdEnabled) {
            Timber.i("Interstitial ad is not enabled for screen: $screenName; country: $countryCode")
            return
        }

        // Don't load if already shown this app session
        if (!interstitialAdTracker.shouldShowInterstitial()) {
            Timber.i("Interstitial ad already shown this app session")
            return
        }

        if (isInterstitialLoaded) {
            Timber.i("Interstitial ad already loaded")
            return
        }

        adProvider.loadInterstitial(
            adUnitId = "ca-app-pub-3940256099942544/1033173712",
            onLoaded = {
                isInterstitialLoaded = true
                showInterstitialAd()
            },
            onFailed = {
                isInterstitialLoaded = false
            }
        )
    }

    fun showInterstitialAd() {
        if (!isInterstitialAdEnabled) {
            Timber.i("Interstitial ad is not enabled")
            return
        }

        if (!interstitialAdTracker.shouldShowInterstitial()) {
            Timber.i("Interstitial ad already shown this app session, skipping")
            return
        }

        if (!isInterstitialLoaded) {
            Timber.w("Interstitial ad not loaded yet, cannot show")
            return
        }

        adProvider.showInterstitial(onAdDismissed = {

        })

        // Mark as shown for entire app session
        interstitialAdTracker.markAsShown()
        isInterstitialLoaded = false

        Timber.d("Interstitial ad shown, marked as shown for app session")
    }

    fun destroy() {
        // Called from Activity/Fragment's onDestroy.
        activeAdViews.values.forEach { it.destroy() }
        activeAdViews.clear()

        // Important: Dispose all cached ComposeViews to prevent leaks.
        shimmerViewCache.forEach { it.disposeComposition() }
        shimmerViewCache.clear()

        isInterstitialLoaded = false
    }
}


/**
 * Class that can be used to reduce width of view with animation
 * @param parentView - is the view to be collapsed
 * @param collapseTill - is the desired height that is to be reached after collapsing
 */
class CollapseHeightAnimation(
    private val parentView: View,
    private val collapseTill: Int = 0,
) : Animation() {

    private val startHeight: Int = parentView.height

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        parentView.layoutParams.height =
            startHeight + ((collapseTill - startHeight) * interpolatedTime).toInt()
        parentView.requestLayout()
    }

    override fun willChangeBounds(): Boolean {
        return true
    }
}