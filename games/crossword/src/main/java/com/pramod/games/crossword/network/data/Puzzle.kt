package com.pramod.games.crossword.network.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class Puzzle(
    @SerializedName("wordId")
    val wordId: String?,
    @SerializedName("answer")
    val answer: String?,
    @SerializedName("clue")
    val clue: String?,
    @SerializedName("col")
    val col: Int?,
    @SerializedName("direction")
    val direction: String?,
    @SerializedName("length")
    val length: Int?,
    @SerializedName("row")
    val row: Int?,
)
