package com.pramod.todaysword.db.remote

import com.pramod.todaysword.db.model.ApiResponse
import com.pramod.todaysword.db.model.WordOfTheDay
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WOTDApiService {
    @GET(EndPoints.GET_WORD_OF_THE_DAY)
    fun getWordOfTheDay(): Call<ApiResponse<WordOfTheDay>>

    @GET(EndPoints.GET_WORDS)
    fun getWords(
        @Query("startFrom") startFrom: String? = null,
        @Query("limit") limit: Int
    ): Call<ApiResponse<List<WordOfTheDay>>>
}