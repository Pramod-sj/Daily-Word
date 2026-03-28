@file:OptIn(ExperimentalMaterial3Api::class)

package com.pramod.games.crossword

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.ComposeView
import com.pramod.dialyword.router.AppRouter
import com.pramod.dialyword.router.routes.CoreRoute
import com.pramod.games.crossword.ui.CrosswordScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class CrosswordActivity : AppCompatActivity() {

    @Inject
    lateinit var appRouter: AppRouter

    private val viewModel: CrosswordViewModel by viewModels()

    companion object {
        const val EXTRA_CROSSWORD_ID = "crosswordId"

        fun newIntent(context: Context, crosswordId: String): Intent {
            return Intent(context, CrosswordActivity::class.java).apply {
                putExtra(EXTRA_CROSSWORD_ID, crosswordId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crossword)
        findViewById<ComposeView>(R.id.compose)
            .setContent {
                CrosswordTheme {
                    CrosswordScreen(
                        viewModel = viewModel,
                        onNavigateBack = ::onNavigateBack,
                        onViewWord = { wordId ->
                            appRouter.navigateTo(
                                context = this@CrosswordActivity,
                                routeUriString = CoreRoute.wordDetailPath(wordId)
                            )
                        }
                    )
                }
            }
    }

    fun onNavigateBack() {
        finish()
    }
}
