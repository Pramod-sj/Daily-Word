package com.pramod.dailyword.framework.datasource.network.service

import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.framework.datasource.network.model.WordNE
import com.pramod.dailyword.framework.datasource.network.model.api.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WordApiService {
    @GET(BuildConfig.GET_WORD_OF_THE_DAY)
    suspend fun getWordOfTheDay(): ApiResponse<WordNE>

    @GET(BuildConfig.GET_WORDS)
    suspend fun getWords(
        @Query("startFrom") startFrom: String? = null,
        @Query("limit") limit: Int
    ): ApiResponse<List<WordNE>>

    @GET(BuildConfig.GET_RANDOM_WORD)
    suspend fun getRandomWord(): ApiResponse<WordNE>
}