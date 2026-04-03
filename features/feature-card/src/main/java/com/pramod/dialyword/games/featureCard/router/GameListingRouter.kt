package com.pramod.dialyword.games.featureCard.router

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.pramod.dialyword.games.featureCard.listing.GameListingActivity
import com.pramod.dialyword.router.FeatureRouter
import com.pramod.dialyword.router.routes.CoreRoute.APP_BASE
import javax.inject.Inject

object GameListingRoute {

    const val GAME_PATH = "games"

    const val GAME_ROUTE = "${APP_BASE}.games"

}

class GameListingRouter @Inject constructor() : FeatureRouter {

    companion object {
        private const val TAG = "GameListingRouter"
    }

    override fun handles(uri: Uri): Boolean {
        // Matches paths like: /games/crossword, /games/quiz
        val isGamesPath = uri.path?.startsWith("/games/") == true

        val hasNoId = uri.getQueryParameter("id") == null

        return isGamesPath && hasNoId
    }

    override fun getNavigationIntent(context: Context, uri: Uri): Intent? {
        if (!handles(uri)) return null

        // Extract "crossword" or "quiz" from app://dailyword.games/games/crossword
        val gameType = uri.lastPathSegment ?: "all"

        return Intent(context, GameListingActivity::class.java).apply {
            putExtra("EXTRA_GAME_TYPE", gameType)
            data = uri
        }
    }
}