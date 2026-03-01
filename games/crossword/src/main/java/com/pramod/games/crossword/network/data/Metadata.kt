package com.pramod.games.crossword.network.data


import com.google.gson.annotations.SerializedName


internal data class Metadata(
    @SerializedName("end_date")
    val endDate: String,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("word_count")
    val wordCount: Int
)