package com.pramod.games.crossword.network.data


import com.google.gson.annotations.SerializedName


internal data class GridInfo(
    @SerializedName("cols")
    val cols: Int,
    @SerializedName("rows")
    val rows: Int
)