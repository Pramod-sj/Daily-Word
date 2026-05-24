package com.pramod.dailyword.core.shared_preference

import android.content.SharedPreferences
import androidx.core.content.edit
import com.pramod.dailyword.core.preferences.RulesPreference
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

internal class SharedPreferencesRulesPreference @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : RulesPreference {

    private fun getRulesKey(gameType: String): String {
        return "has_seen_rules_${gameType.lowercase()}"
    }

    override fun isFirstTimePlayer(gameType: String): Flow<Boolean> = callbackFlow {
        val key = getRulesKey(gameType)

        // 1. Emit the initial value immediately when the flow is collected
        // If the key doesn't exist, it defaults to false. We invert it (!) so it returns true (IS first time).
        trySend(!sharedPreferences.getBoolean(key, false))

        // 2. Set up a listener for future changes
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, changedKey ->
            if (changedKey == key) {
                trySend(!prefs.getBoolean(key, false))
            }
        }

        // 3. Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        // 4. Clean up the listener when the Flow collection is cancelled (e.g., ViewModel cleared)
        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
    .distinctUntilChanged() // Prevents emitting the same value twice in a row
    .conflate() // Optimizes backpressure if changes happen too quickly

    override suspend fun markRulesAsSeen(gameType: String) {
        // The core-ktx .edit() function defaults to commit = false, 
        // which uses apply() under the hood to write asynchronously.
        sharedPreferences.edit {
            putBoolean(getRulesKey(gameType), true)
        }
    }
}