package com.pramod.games.crossword.router

import com.pramod.dialyword.router.routes.CoreRoute.APP_BASE

object CrosswordRoute {

    const val CROSSWORD_PATH = "crossword"
    private const val CROSSWORD_ROUTE = "${APP_BASE}.games/$CROSSWORD_PATH"

    fun crosswordGameRoute(puzzleId: String): String {
        return "$CROSSWORD_ROUTE?id=$puzzleId"
    }

}