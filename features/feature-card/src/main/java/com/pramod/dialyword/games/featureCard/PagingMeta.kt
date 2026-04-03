package com.pramod.dialyword.games.featureCard


import com.google.gson.annotations.SerializedName

data class PagingMeta(
    @SerializedName("page_size")
    val pageSize: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_records")
    val totalRecords: Int
)