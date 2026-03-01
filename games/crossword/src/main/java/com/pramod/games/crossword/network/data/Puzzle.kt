package com.pramod.games.crossword.network.data

import com.google.gson.annotations.SerializedName

internal data class Puzzle(
    @SerializedName("answer")
    val answer: String,
    @SerializedName("clue")
    val clue: String,
    @SerializedName("col")
    val col: Int,
    @SerializedName("direction")
    val direction: String,
    @SerializedName("length")
    val length: Int,
    @SerializedName("row")
    val row: Int,
)
