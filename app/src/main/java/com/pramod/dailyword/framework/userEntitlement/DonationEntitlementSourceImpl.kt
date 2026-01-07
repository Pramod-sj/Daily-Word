package com.pramod.dailyword.framework.userEntitlement

import androidx.lifecycle.asFlow
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * An [EntitlementSource] that determines user entitlements based on past donations.
 *
 * This class checks for specific donation items that grant premium features, such as an ad-free
 * experience. It cross-references the user's donated items (stored in [PrefManager]) with a list
 * of premium donation SKUs fetched from Firebase Remote Config.
 *
 * @property prefManager The preference manager to access stored donation information.
 * @property fbRemoteConfig The Firebase Remote Config client to get the list of premium donation item IDs.
 */
class DonationEntitlementSource @Inject constructor(
    private val prefManager: PrefManager,
    private val fbRemoteConfig: FBRemoteConfig
) : EntitlementSource {

    override fun getEntitlements(): Flow<Set<Entitlement>> =
        prefManager.getDonatedItems().asFlow()
            .map { donatedItems ->
                if (
                    prefManager.hasDonated() == true &&
                    donatedItems.orEmpty().any {
                        it in fbRemoteConfig
                            .getPremiumDonationItems()
                            .premiumDonationItemIds.orEmpty()
                    }
                ) {
                    setOf(Entitlement.AdFree)
                } else {
                    emptySet()
                }
            }
}
