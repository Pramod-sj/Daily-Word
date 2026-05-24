package com.pramod.dailyword.games.results

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_results")
data class GameResultEntity(
    @PrimaryKey
    val puzzleId: String,
    val gameType: String,
    val completionTimeMillis: Long,
    val score: Int,
    val scoreTier: String,
    val gameData: String? = null, // Generic JSON string for any game-specific data (e.g. user answers, revealed cells)
    val solvedDateMillis: Long = System.currentTimeMillis()
)
