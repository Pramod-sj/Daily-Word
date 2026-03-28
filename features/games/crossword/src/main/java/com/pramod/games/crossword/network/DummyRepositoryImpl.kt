package com.pramod.games.crossword.network

import android.content.Context
import com.google.gson.Gson
import com.pramod.dailyword.games.results.GameResultEntity
import com.pramod.games.crossword.Resource
import com.pramod.games.crossword.network.data.CrosswordResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DummyRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val gson: Gson,
) : CrosswordRepository {
    override suspend fun getCrossword(crosswordId: String): Resource<CrosswordResponse?> =
        withContext(Dispatchers.IO) {
            val puzzle =
                context.assets
                    .open("dummy_puzzle.json")
                    .bufferedReader()
                    .use { it.readText() }
            val crosswordResponse = gson.fromJson(puzzle, CrosswordResponse::class.java)
            Resource.success(crosswordResponse)
        }

    override suspend fun saveCrosswordResult(result: GameResultEntity) {
        TODO("Not yet implemented")
    }

    override fun getCrosswordResult(puzzleId: String): Flow<GameResultEntity?> {
        TODO("Not yet implemented")
    }

    override fun getAllCrosswordResults(): Flow<List<GameResultEntity>> {
        TODO("Not yet implemented")
    }
}
