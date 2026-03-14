package com.pramod.games.crossword.network

import com.pramod.games.crossword.network.data.CrosswordResponse
import retrofit2.http.GET
import retrofit2.http.Query

internal interface CrossWordApiService {

    @GET("Crossword/getPuzzle")
    suspend fun getCrossword(
        @Query("id") id: String
    ): CrosswordResponse

}
