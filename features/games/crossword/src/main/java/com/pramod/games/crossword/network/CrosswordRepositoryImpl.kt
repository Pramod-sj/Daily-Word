package com.pramod.games.crossword.network

import com.pramod.dailyword.games.results.GameResultEntity
import com.pramod.dailyword.games.results.GameResultRepository
import com.pramod.games.crossword.Resource
import com.pramod.games.crossword.network.data.CrosswordResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

internal class CrosswordRepositoryImpl @Inject constructor(
    private val crossWordApiService: CrossWordApiService,
    private val gameResultRepository: GameResultRepository
) : CrosswordRepository {

    override suspend fun getCrossword(crosswordId: String): Resource<CrosswordResponse?> {
        val apiResult = safeApiCall(Dispatchers.Default) {
            crossWordApiService.getCrossword(id = crosswordId)
        }
        return when (apiResult) {
            is ApiResult.GenericError -> Resource.error(Throwable(apiResult.message))
            is ApiResult.NetworkError -> Resource.error(Throwable(apiResult.message))
            is ApiResult.Success -> {
                val response = apiResult.data
                val localResult = gameResultRepository.getResult(crosswordId).firstOrNull()
                Resource.success(response?.copy(result = localResult))
            }
        }
    }

    override suspend fun saveCrosswordResult(result: GameResultEntity) {
        gameResultRepository.saveResult(result)
    }

    override fun getCrosswordResult(puzzleId: String): Flow<GameResultEntity?> {
        return gameResultRepository.getResult(puzzleId)
    }

    override fun getAllCrosswordResults(): Flow<List<GameResultEntity>> {
        return gameResultRepository.getAllResults()
    }

}