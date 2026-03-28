package com.pramod.dailyword.games.common.game_rules.model

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

@Immutable
data class GameRule(
    @SerializedName("icon_name")
    val iconName: String,
    val title: String,
    val description: String
)