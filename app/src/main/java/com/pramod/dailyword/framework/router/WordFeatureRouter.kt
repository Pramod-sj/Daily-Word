package com.pramod.dailyword.framework.router

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.pramod.dailyword.framework.ui.bookmarks.FavoriteWordsActivity
import com.pramod.dailyword.framework.ui.home.HomeActivity
import com.pramod.dailyword.framework.ui.recap.RecapWordsActivity
import com.pramod.dailyword.framework.ui.worddetails.WordDetailedActivity
import com.pramod.dailyword.framework.ui.words.WordListActivity
import com.pramod.dialyword.router.routes.CoreRoute
import com.pramod.dialyword.router.FeatureRouter
import com.pramod.dialyword.router.routes.CorePath
import javax.inject.Inject

class WordFeatureRouter @Inject constructor() : FeatureRouter {

    override fun handles(uri: Uri): Boolean {
        return uri.scheme == CoreRoute.APP_SCHEME && uri.host == CoreRoute.APP_HOST
    }

    override fun getNavigationIntent(
        context: Context,
        uri: Uri
    ): Intent? {
        return when (uri.path) {

            CorePath.HOME -> {
                HomeActivity.newIntent(context = context)
            }

            CorePath.WORD_DETAIL -> {
                uri.getQueryParameter("wordDate")?.let { date ->
                    WordDetailedActivity.newIntent(
                        context = context,
                        wordDate = date,
                        word = null
                    )
                }
            }

            CorePath.WORD_LIST -> WordListActivity.newIntent(context = context)

            CorePath.FAVORITE_WORDS -> FavoriteWordsActivity.newIntent(context = context)

            CorePath.RECAP_WORDS -> RecapWordsActivity.newIntent(context = context)

            else -> null
        }
    }

}