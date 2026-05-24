package com.pramod.games.crossword.network.data


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


@Keep
internal data class GridInfo(
    @SerializedName("cols")
    val cols: Int,
    @SerializedName("rows")
    val rows: Int
)