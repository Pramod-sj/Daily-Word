package com.pramod.games.crossword.network

import com.pramod.games.crossword.network.data.CrosswordResponse
import retrofit2.http.GET

internal interface CrossWordApiService {

    @GET("/getWeeklyPuzzle")
    suspend fun getWeeklyPuzzle(): CrosswordResponse

}
