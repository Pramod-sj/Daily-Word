package com.pramod.games.crossword.network

import com.pramod.games.crossword.Resource
import com.pramod.games.crossword.network.data.CrosswordResponse

internal interface CrosswordRepository {

    suspend fun getCrossword(crosswordId: String): Resource<CrosswordResponse?>

}