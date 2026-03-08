package com.pramod.dialyword.games.core

import retrofit2.Response
import retrofit2.http.GET

interface GamesApiService {

    // Replace with your actual endpoint path
    @GET("api/v1/games/live-features")
    suspend fun getLiveFeatures(): Response<FeatureCardResponse>
}