package com.pramod.games.crossword.router

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.pramod.dialyword.router.routes.CoreRoute.APP_HOST
import com.pramod.dialyword.router.routes.CoreRoute.APP_SCHEME
import com.pramod.dialyword.router.FeatureRouter
import com.pramod.games.crossword.CrosswordActivity
import javax.inject.Inject

internal class GamesFeatureRouter @Inject constructor() : FeatureRouter {

    override fun handles(uri: Uri): Boolean {
        return uri.scheme == APP_SCHEME && uri.host == "$APP_HOST.games"
    }

    override fun getNavigationIntent(context: Context, uri: Uri): Intent? {
        return when (uri.path?.removePrefix("/")) {
            CrosswordRoute.CROSSWORD_PATH -> {
                val crosswordId = uri.getQueryParameter("id") ?: return null
                CrosswordActivity.newIntent(context, crosswordId)
            }

            else -> null
        }
    }
}