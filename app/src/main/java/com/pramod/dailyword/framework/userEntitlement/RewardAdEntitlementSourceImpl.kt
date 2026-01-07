package com.pramod.dailyword.framework.userEntitlement

import androidx.lifecycle.asFlow
import com.pramod.dailyword.framework.helper.ads.rewards.RewardedAdsManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * An [EntitlementSource] that provides entitlements based on rewarded ad views.
 *
 * This class observes the state from [RewardedAdsManager] to determine if the user
 * has an active "ad-free" entitlement. When a user watches a rewarded ad, the
 * manager grants them a temporary period where ads are disabled. This source
 * translates that temporary state into an [Entitlement.AdFreeUntil] object.
 *
 * @param rewardedAdsManager The manager responsible for handling rewarded ads and tracking
 *                           the expiry time for ad-free periods.
 */
class RewardAdEntitlementSource @Inject constructor(
    private val rewardedAdsManager: RewardedAdsManager
) : EntitlementSource {

    override fun getEntitlements(): Flow<Set<Entitlement>> =
        rewardedAdsManager.getAdsDisabledUntil()
            .asFlow()
            .map { expiry ->
                if (expiry > System.currentTimeMillis())
                    setOf(Entitlement.AdFreeUntil(expiry))
                else emptySet()
            }

}
