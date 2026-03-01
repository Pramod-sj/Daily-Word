package com.pramod.games.crossword.network.data

import com.google.gson.annotations.SerializedName

internal data class CrosswordResponse(
    @SerializedName("grid")
    val gridInfo: GridInfo,
    @SerializedName("metadata")
    val metadata: Metadata,
    @SerializedName("puzzle")
    val puzzle: List<Puzzle>,
    @SerializedName("status")
    val status: String,
)
