package com.pramod.dailyword.db.remote

import com.pramod.dailyword.db.model.ApiResponse
import com.pramod.dailyword.db.model.WordOfTheDay
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WOTDApiService {
    @GET(EndPoints.GET_WORD_OF_THE_DAY)
    suspend fun getWordOfTheDay(): ApiResponse<WordOfTheDay?>?

    @GET(EndPoints.GET_WORDS)
    suspend fun getWords(
        @Query("startFrom") startFrom: String? = null,
        @Query("limit") limit: Int
    ): ApiResponse<List<WordOfTheDay>?>?
}