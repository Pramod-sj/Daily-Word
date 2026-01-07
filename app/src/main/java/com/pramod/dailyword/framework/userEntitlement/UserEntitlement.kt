package com.pramod.dailyword.framework.userEntitlement

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserEntitlement @Inject constructor(
    sources: Set<@JvmSuppressWildcards EntitlementSource>
) {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val entitlements: StateFlow<Set<Entitlement>> =
        combine(*sources.map { it.getEntitlements() }
            .toTypedArray()) { sets: Array<Set<Entitlement>> ->
            sets.toList().flatten().toSet()
        }.stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = emptySet()
        )

    val isAdFree = entitlements.map { entitlements ->
        entitlements.any {
            it is Entitlement.AdFree || it is Entitlement.AdFreeUntil
        }
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    fun has(entitlement: Entitlement): Boolean =
        entitlements.value.any { it == entitlement }

}
