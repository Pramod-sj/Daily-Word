package com.pramod.games.crossword.network

import com.pramod.dailyword.games.results.GameResultEntity
import com.pramod.games.crossword.Resource
import com.pramod.games.crossword.network.data.CrosswordResponse
import kotlinx.coroutines.flow.Flow

internal interface CrosswordRepository {

    suspend fun getCrossword(crosswordId: String): Resource<CrosswordResponse?>

    suspend fun saveCrosswordResult(result: GameResultEntity)

    fun getCrosswordResult(puzzleId: String): Flow<GameResultEntity?>

    fun getAllCrosswordResults(): Flow<List<GameResultEntity>>

}