package com.pramod.games.crossword.network

import com.pramod.games.crossword.Resource
import com.pramod.games.crossword.network.data.CrosswordResponse
import kotlinx.coroutines.Dispatchers

internal class CrosswordRepositoryImpl constructor(
    private val crossWordApiService: CrossWordApiService
) : CrosswordRepository {

    override suspend fun getWeeklyPuzzle(): Resource<CrosswordResponse?> {
        val apiResult = safeApiCall(Dispatchers.Default) {
            crossWordApiService.getWeeklyPuzzle()
        }
        return when (apiResult) {
            is ApiResult.GenericError -> Resource.error(Throwable(apiResult.message))
            is ApiResult.NetworkError -> Resource.error(Throwable(apiResult.message))
            is ApiResult.Success -> Resource.success(apiResult.data)
        }
    }

}