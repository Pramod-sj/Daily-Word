package com.pramod.dialyword.games.featureCard

import com.pramod.dialyword.games.featureCard.dismissible.DismissRecord
import com.pramod.dialyword.games.featureCard.dismissible.DismissedCardsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

internal class DismissalRepository @Inject constructor(
    private val store: DismissedCardsStore
) {

    private val sessionIds = MutableStateFlow<Set<String>>(emptySet())

    val dismissedIds: StateFlow<Set<String>>
        get() = combine(
            sessionIds,
            store.activeRecords()
        ) { session, records ->
            session + records.map { it.cardId }.toSet()
        }.stateIn(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            started = SharingStarted.Eagerly,
            initialValue = emptySet()
        )

    suspend fun dismiss(card: FeatureCard) {
        val config = card.dismissConfig?.takeIf { it.enabled == true } ?: return

        val cardId = card.id ?: return

        // Optimistic — instant UI update regardless of scope
        sessionIds.update { it + cardId }

        when (config.scopeEnum) {
            DismissScope.SESSION -> {
                // session set is the final store, nothing else to do
            }

            DismissScope.PERSISTENT -> {
                store.save(
                    DismissRecord(
                        cardId = cardId,
                        scope = DismissScope.PERSISTENT
                    )
                )
                // DataStore flow now owns it, remove from session
                sessionIds.update { it - cardId }
            }

            DismissScope.TIMED -> {
                val ttl = config.ttlHours ?: 24
                store.save(
                    DismissRecord(
                        cardId = cardId,
                        scope = DismissScope.TIMED,
                        expiresAt = System.currentTimeMillis() + ttl.hoursToMillis()
                    )
                )
                sessionIds.update { it - cardId }
            }
        }
    }

    suspend fun prune() = store.prune()

    private fun Int.hoursToMillis() = this * 60 * 60 * 1000L
}