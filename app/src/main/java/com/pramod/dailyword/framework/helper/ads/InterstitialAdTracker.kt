package com.pramod.dailyword.framework.helper.ads


import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.firebase.model.AdsConfig
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks user actions and determines when an interstitial ad
 * should be shown during a single app session.
 *
 * This class:
 * - Uses Firebase Remote Config–driven values
 * - Limits how frequently interstitials are shown
 * - Enforces a maximum number of interstitials per session
 *
 * Expected usage flow:
 * 1. Call [incrementActionCount] on each qualifying user action
 * 2. Call [shouldShowInterstitial] before attempting to show an ad
 * 3. Call [markAsShown] after an interstitial is successfully shown
 * 4. Call [resetSession] when a new app session starts
 */
@Singleton
class InterstitialAdTracker @Inject constructor(
    private val fbRemoteConfig: FBRemoteConfig
) {

    /**
     * Ads-related configuration fetched from Firebase Remote Config.
     */
    private val adsConfig: AdsConfig get() = fbRemoteConfig.getAdsConfig()

    /**
     * Number of user actions required before an interstitial becomes eligible.
     * Example: show an interstitial after every N actions.
     */
    private val actionsBeforeAd get() = adsConfig.actionCountForInterstitial

    /**
     * Maximum number of interstitials allowed per app session.
     */
    private val maxInterstitialsPerSession get() = adsConfig.maxInterstitialsPerSession

    /**
     * Tracks the number of user actions performed since the last interstitial.
     */
    private var actionCount = 0

    /**
     * Tracks how many interstitials have been shown in the current session.
     */
    private var interstitialShownCount = 0


    private val _showInterstitial = Channel<Unit>(Channel.BUFFERED)
    val showInterstitial: Flow<Unit> = _showInterstitial.receiveAsFlow()


    /**
     * Increments the action counter used to decide when to show an interstitial.
     *
     * If the session limit has already been reached, this method does nothing.
     * This prevents unnecessary counting once ads are no longer allowed.
     */
    fun incrementActionCount() {
        if (hasReachedSessionLimit()) return

        actionCount++

        if (shouldShowInterstitial()) _showInterstitial.trySend(Unit)

        Timber.d(
            "Action count: $actionCount " +
                "(Interstitials shown: $interstitialShownCount/$maxInterstitialsPerSession)"
        )
    }

    /**
     * Determines whether an interstitial ad should be shown.
     *
     * Conditions:
     * - Session interstitial limit must NOT be reached
     * - Required number of actions must be completed
     *
     * @return true if an interstitial is eligible to be shown, false otherwise
     */
    fun shouldShowInterstitial(): Boolean {
        val shouldShow =
            !hasReachedSessionLimit() && actionCount >= (actionsBeforeAd ?: 5)

        Timber.i(
            "shouldShowInterstitial: $shouldShow " +
                "(shown: $interstitialShownCount/$maxInterstitialsPerSession, " +
                "actions: $actionCount/$actionsBeforeAd)"
        )

        return shouldShow
    }

    /**
     * Marks an interstitial as successfully shown.
     *
     * This:
     * - Increments the shown interstitial count
     * - Resets the action counter for the next interstitial cycle
     *
     * Call this ONLY after the ad has actually been displayed.
     */
    fun markAsShown() {
        interstitialShownCount++
        actionCount = 0

        Timber.d(
            "Interstitial marked as shown. " +
                "Total shown: $interstitialShownCount/$maxInterstitialsPerSession"
        )
    }

    /**
     * Checks whether the maximum number of interstitials
     * for the current session has been reached.
     */
    fun hasReachedSessionLimit(): Boolean {
        return interstitialShownCount >= (maxInterstitialsPerSession ?: 1)
    }

    /**
     * @return Number of interstitials already shown in this session.
     */
    fun getInterstitialShownCount(): Int = interstitialShownCount

    /**
     * @return Number of remaining interstitials that can be shown
     * in the current session.
     */
    fun getRemainingInterstitials(): Int {
        return maxOf(0, (maxInterstitialsPerSession ?: 1) - interstitialShownCount)
    }

    /**
     * Resets all counters for a new app session.
     *
     * Should be called on:
     * - App cold start
     * - Explicit session reset logic (if any)
     */
    fun resetSession() {
        actionCount = 0
        interstitialShownCount = 0
        Timber.d("Interstitial tracker session reset")
    }
}
