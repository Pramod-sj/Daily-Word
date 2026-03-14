package com.pramod.games.crossword.network.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.pramod.dailyword.games.results.GameResultEntity

@Keep
internal data class CrosswordResponse(
    @SerializedName("grid")
    val gridInfo: GridInfo? = null,
    @SerializedName("metadata")
    val metadata: Metadata? = null,
    @SerializedName("puzzle")
    val puzzle: List<Puzzle>? = null,
    @SerializedName("status")
    val status: String? = null,

    val result: GameResultEntity? = null
)
