package com.pramod.dailyword.games.results

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GameResultRepositoryImpl @Inject constructor(
    private val gameResultDao: GameResultDao
) : GameResultRepository {

    override suspend fun saveResult(result: GameResultEntity) {
        withContext(Dispatchers.IO) {
            gameResultDao.insertResult(result)
        }
    }

    override fun getResult(puzzleId: String): Flow<GameResultEntity?> {
        return gameResultDao.getResultById(puzzleId)
    }

    override fun getAllResults(): Flow<List<GameResultEntity>> {
        return gameResultDao.getAllResults()
    }
}
