package com.pramod.dialyword.games.featureCard

import retrofit2.Response
import retrofit2.http.GET

interface GamesApiService {

    // Replace with your actual endpoint path
    @GET("Games/getLiveGames")
    suspend fun getLiveFeatures(): Response<FeatureCardResponse>
}