package com.pramod.games.crossword.network

import android.content.Context
import com.google.gson.Gson
import com.pramod.games.crossword.Resource
import com.pramod.games.crossword.network.data.CrosswordResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class DummyRepositoryImpl constructor(
    private val context: Context,
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
}
