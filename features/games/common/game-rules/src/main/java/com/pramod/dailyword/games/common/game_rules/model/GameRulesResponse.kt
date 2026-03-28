package com.pramod.dailyword.games.common.game_rules.model

import com.google.gson.annotations.SerializedName

data class GameRulesResponse(
    @SerializedName("game_type")
    val gameType: String,
    val instructions: List<GameRule>
)