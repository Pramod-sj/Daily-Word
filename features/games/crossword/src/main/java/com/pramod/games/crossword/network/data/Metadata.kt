package com.pramod.games.crossword.network.data


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
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