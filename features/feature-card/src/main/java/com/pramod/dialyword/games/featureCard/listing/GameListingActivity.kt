package com.pramod.dialyword.games.featureCard.listing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pramod.dialyword.games.featureCard.CrosswordTheme
import com.pramod.dialyword.games.featureCard.listing.ui.GameList
import com.pramod.dialyword.games.featureCard.listing.ui.GameListingTopBar
import com.pramod.dialyword.router.AppRouter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GameListingActivity : AppCompatActivity() {

    companion object {
        fun newInstance(context: Context): Intent {
            return Intent(context, GameListingActivity::class.java)
        }
    }

    @Inject
    lateinit var appRouter: AppRouter

    private val viewModel by viewModels<GameListingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ComposeView(this).apply {
            setContent {
                CrosswordTheme {
                    GameListingScreen()
                }
            }
        })
    }

    @Composable
    fun GameListingScreen() {

        val filter by viewModel.filter.collectAsStateWithLifecycle()

        val gameListing by viewModel.games.collectAsStateWithLifecycle()

        val initialPage by viewModel.initialPage.collectAsStateWithLifecycle()

        val completedCount by viewModel.completedCount.collectAsStateWithLifecycle()

        Scaffold(
            topBar = {
                GameListingTopBar(
                    completedCount = completedCount,
                    totalCount = initialPage?.paginationInfo?.totalRecords ?: 0,
                    filter = filter,
                    onFilterChange = viewModel::setFilter,
                    onBack = {
                        onBackPressedDispatcher.onBackPressed()
                    },
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                GameList(
                    games = gameListing,
                    onGameClick = { gameCard ->
                        appRouter.navigateTo(
                            context = this@GameListingActivity,
                            routeUriString = gameCard.gameRoute
                        )
                    }
                )
            }
        }

    }

}

