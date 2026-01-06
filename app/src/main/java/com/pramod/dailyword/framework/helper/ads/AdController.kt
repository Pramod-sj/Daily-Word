package com.pramod.dailyword.framework.helper.ads

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.map
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.helper.ads.rewards.RewardedAdsManager
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import com.pramod.dailyword.framework.ui.common.ScreenNameProvider
import com.pramod.dailyword.framework.util.NetworkUtils
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import java.util.LinkedList
import javax.inject.Inject

@ActivityScoped
class AdController @Inject constructor(
    private val adProvider: AdProvider,
    private val fbRemoteConfig: FBRemoteConfig,
    private val prefManager: PrefManager,
    private val myActivity: Activity,
    private val interstitialAdTracker: InterstitialAdTracker,
    private val rewardedAdsManager: RewardedAdsManager,
) {
    private val activity = myActivity as ComponentActivity

    private val currentScreenName = (activity as? ScreenNameProvider)?.screenName ?: "Unknown"

    private val countryCode = prefManager.getCountryCode()

    private val adConfig = fbRemoteConfig.getAdsConfig()

    val isAdEnabled: StateFlow<Boolean> =
        rewardedAdsManager.areAdsDisabled().map { areAdsDisabled ->
            adConfig.adsEnabled && !areAdsDisabled
        }.asFlow().stateIn(
            scope = activity.lifecycleScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val isBannerAdEnabled: StateFlow<Boolean> = isAdEnabled.map { isAdEnabled ->
        isAdEnabled &&
            adConfig.adsEnabledCountries.contains(countryCode) &&
            adConfig.adsEnabledScreen[currentScreenName]?.banner ?: false
    }.stateIn(
        scope = activity.lifecycleScope, started = SharingStarted.Eagerly, initialValue = false
    )


    val isInterstitialAdEnabled: StateFlow<Boolean> = isAdEnabled.map { isAdEnabled ->
        isAdEnabled &&
            adConfig.adsEnabledCountries.contains(countryCode) &&
            adConfig.adsEnabledScreen[currentScreenName]?.interstitial ?: false
    }.stateIn(
        scope = activity.lifecycleScope, started = SharingStarted.Eagerly, initialValue = false
    )

    val showDoNotShowAdsDialogAfterInterstitial: Boolean
        get() = adConfig.adsEnabledScreen[currentScreenName]?.showPostInterstitialDialog
            ?: false


    // Cache for loaded native ad views. Keyed by the container they are in.
    private val activeAdViews = mutableMapOf<ViewGroup, AdViewWrapper>()

    // Cache for shimmer placeholder views. A simple queue is effective for RecyclerView.
    private val shimmerViewCache = LinkedList<ComposeView>()

    private var isInterstitialLoaded = false

    private var isRewardedAdLoaded = false

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

        if (!NetworkUtils.isNetworkActive(activity)) {
            Timber.i("Network not available, Not loading Banner ads")
            return
        }

        if (!isBannerAdEnabled.value) {
            Timber.i("Banner ad is not enabled for screen: $currentScreenName; country: $countryCode")
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
            })
    }

    fun hideBanner(container: ViewGroup) {
        if (!isBannerAdEnabled.value) {
            Timber.i("Banner ad is not enabled for screen: $currentScreenName; country: $countryCode")
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

        if (!NetworkUtils.isNetworkActive(activity)) {
            Timber.i("Network not available, Not loading Interstitial ad")
            return
        }

        if (!isInterstitialAdEnabled.value) {
            Timber.i("Interstitial ad is not enabled for screen: $currentScreenName; country: $countryCode")
            return
        }

        // Don't load if already shown this app session
        if (interstitialAdTracker.hasReachedSessionLimit()) {
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
            },
            onFailed = {
                isInterstitialLoaded = false
            })
    }

    fun showInterstitialAd(onAdDismissed: () -> Unit) {
        if (!isInterstitialAdEnabled.value) {
            Timber.i("Interstitial ad is not enabled")
            return
        }

        if (!interstitialAdTracker.shouldShowInterstitial()) {
            Timber.i("Interstitial ad either already shown for this app session or action criteria not met")
            return
        }

        if (!isInterstitialLoaded) {
            Timber.w("Interstitial ad not loaded yet, cannot show")
            return
        }

        adProvider.showInterstitial(onAdDismissed = {
            // preload interstitial ad again if still eligible
            loadInterstitialAd()
            onAdDismissed()
        })

        // Mark as shown for entire app session
        interstitialAdTracker.markAsShown()
        isInterstitialLoaded = false

        Timber.d("Interstitial ad shown, marked as shown for app session")
    }


    fun loadRewardedAd(
        adUnitId: String = "ca-app-pub-3940256099942544/5224354917",
        onLoaded: () -> Unit = {},
        onFailed: (Throwable) -> Unit = {}
    ) {
        if (!NetworkUtils.isNetworkActive(activity)) {
            Timber.i("Network not available, Not loading Rewarded ad")
            return
        }

        if (!isAdEnabled.value) {
            Timber.i("Ads is not enabled")
            onFailed(Throwable("Ads is not enabled"))
            return
        }

        // If ads already disabled via reward, don't load
        if (rewardedAdsManager.areAdsDisabled().value == true) {
            Timber.i("Ads already disabled via reward, skipping rewarded ad load")
            onFailed(Throwable("Ads already disabled via reward, skipping rewarded ad load"))
            return
        }

        adProvider.loadRewardedAd(adUnitId = adUnitId, onLoaded = {
            isRewardedAdLoaded = true
            Timber.d("Rewarded ad loaded")
            onLoaded()
        }, onFailed = {
            isRewardedAdLoaded = false
            Timber.e("Rewarded ad failed to load")
            onFailed(it)
        })
    }


    fun showRewardedAd(
        onRewardGranted: () -> Unit = {}, onDismissed: () -> Unit = {}
    ) {

        if (!isRewardedAdLoaded) {
            Timber.w("Rewarded ad not loaded")
            return
        }

        adProvider.showRewardedAd(onUserEarnedReward = {
            Timber.d("User earned rewarded ad benefit")
            // Disable ads for next x days
            rewardedAdsManager.onRewardAdCompleted()
            onRewardGranted()
        }, onAdDismissed = {
            isRewardedAdLoaded = false
            onDismissed()
        })

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

    fun ComponentActivity.canActivityShowAd(): Boolean {
        return !isFinishing &&
            !isDestroyed &&
            lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
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