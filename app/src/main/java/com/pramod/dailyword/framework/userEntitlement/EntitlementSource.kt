package com.pramod.dailyword.framework.userEntitlement

import kotlinx.coroutines.flow.Flow


/**
 * Defines a contract for sources that provide user entitlements.
 *
 * Implementations of this interface are responsible for fetching or calculating
 * the set of entitlements a user currently possesses. This could involve
 * checking a remote server, a local database, or in-app purchase receipts.
 */
interface EntitlementSource {

    fun getEntitlements(): Flow<Set<Entitlement>>

}


