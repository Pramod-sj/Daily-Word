package com.pramod.games.crossword.network

import com.pramod.games.crossword.network.data.CrosswordResponse
import retrofit2.http.GET

internal interface CrossWordApiService {

    @GET("/getCrossword")
    suspend fun getCrossword(id: String): CrosswordResponse

}
