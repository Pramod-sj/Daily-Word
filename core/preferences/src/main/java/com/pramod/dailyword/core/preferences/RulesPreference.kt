package com.pramod.dailyword.core.preferences

import kotlinx.coroutines.flow.Flow

interface RulesPreference {

    /** Returns true if the user has NEVER seen the rules for this game type. */
    fun isFirstTimePlayer(gameType: String): Flow<Boolean>

    /** Marks the rules as seen for this specific game type. */
    suspend fun markRulesAsSeen(gameType: String)

}