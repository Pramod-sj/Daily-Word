package com.pramod.dialyword.games.featureCard

import retrofit2.Response
import retrofit2.http.GET

internal interface GamesApiService {

    @GET("Games/getLiveGames")
    suspend fun getLiveFeatures(): Response<FeatureCardResponse>

}