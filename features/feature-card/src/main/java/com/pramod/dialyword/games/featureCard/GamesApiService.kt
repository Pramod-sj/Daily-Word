package com.pramod.dialyword.games.featureCard

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

internal interface GamesApiService {

    @GET("Games/getLiveGames")
    suspend fun getLiveFeatures(): Response<FeatureCardResponse>

    @GET("Games/getAllGames")
    suspend fun getAllFeatures(
        @Query("page_number") pageNumber: Int,
        @Query("page_size") pageSize: Int,
    ): Response<FeatureCardResponse>

}