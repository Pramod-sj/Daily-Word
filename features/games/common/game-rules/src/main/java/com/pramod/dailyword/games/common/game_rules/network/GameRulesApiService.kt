package com.pramod.dailyword.games.common.game_rules.network

import com.pramod.dailyword.games.common.game_rules.model.GameRulesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GameRulesApiService {

    @GET("GameRules/getInstructions")
    suspend fun getGameRules(
        @Query("game_type") gameType: String
    ): Response<GameRulesResponse>

}