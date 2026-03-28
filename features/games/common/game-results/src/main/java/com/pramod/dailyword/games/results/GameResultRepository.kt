package com.pramod.dailyword.games.results

import kotlinx.coroutines.flow.Flow

interface GameResultRepository {

    suspend fun saveResult(result: GameResultEntity)

    fun getResult(puzzleId: String): Flow<GameResultEntity?>

    fun getAllResults(): Flow<List<GameResultEntity>>

}
