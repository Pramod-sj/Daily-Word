package com.pramod.dailyword.games.common.game_rules.repo

import com.pramod.dailyword.games.common.game_rules.model.GameRule
import com.pramod.dailyword.games.common.game_rules.network.GameRulesApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRulesRepository @Inject constructor(
    private val apiService: GameRulesApiService
) {

    suspend fun fetchRules(gameType: String): Result<List<GameRule>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getGameRules(gameType)
                val body = response.body()

                if (response.isSuccessful && body != null) {
                    Result.success(body.instructions)
                } else {
                    Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

}