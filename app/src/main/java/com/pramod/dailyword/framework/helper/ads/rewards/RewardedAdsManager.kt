package com.pramod.dailyword.framework.helper.ads.rewards

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardedAdsManager @Inject constructor(
    private val fbRemoteConfig: FBRemoteConfig,
    private val prefManager: PrefManager
) {

    companion object {
        private const val MILLIS_IN_DAY = 24 * 60 * 60 * 1000L
    }

    private val disableAdsForDays: Int get() = fbRemoteConfig.getAdsConfig().disabledAdsDays ?: 7

    /**
     * Call this when the rewarded ad is COMPLETELY watched.
     * This will disable ads for the next 7 days.
     */
    fun onRewardAdCompleted() {
        val disableUntil = System.currentTimeMillis() +
            (disableAdsForDays * MILLIS_IN_DAY)
        prefManager.setAdsDisabledUntil(disableUntil)
    }

    /**
     * Returns true if ads should currently be disabled
     * due to a rewarded-ad benefit.
     */
    fun areAdsDisabled(): LiveData<Boolean> {
        return prefManager.getAdsDisabledUntil().map { disabledUntil ->
            System.currentTimeMillis() < disabledUntil
        }
    }

    fun getAdsDisabledUntil(): LiveData<Long> {
        return prefManager.getAdsDisabledUntil()
    }

    /**
     * Returns remaining time in milliseconds
     * Useful if you want to show "X days left" in UI.
     */
    fun getRemainingDisableTimeMillis(): LiveData<Long> {
        return prefManager.getAdsDisabledUntil().map { disabledUntil ->
            maxOf(0L, disabledUntil - System.currentTimeMillis())
        }
    }

    /**
     * Clears the rewarded-ad benefit.
     * Useful for debugging or logout.
     */
    fun clearReward() {
        prefManager.clearAdsDisabledUntil()
    }
}