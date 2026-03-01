@file:OptIn(ExperimentalMaterial3Api::class)

package com.pramod.games.crossword

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pramod.games.crossword.ui.ClueBanner
import com.pramod.games.crossword.ui.CrosswordTopAppBar
import com.pramod.games.crossword.ui.DynamicCrosswordGrid
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CrosswordActivity : AppCompatActivity() {
    private val viewModel: CrosswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crossword)
        findViewById<ComposeView>(R.id.compose)
            .setContent {
                CrosswordTheme {
                    var showExitDialog by remember { mutableStateOf(false) }

                    val result by viewModel.puzzleResult.collectAsState()
                    val isPuzzleComplete = result != null

                    BackHandler(!isPuzzleComplete) {
                        showExitDialog = true
                    }

                    Scaffold(
                        topBar = {
                            CrosswordTopAppBar(
                                viewModel = viewModel,
                                onBackClick = {
                                    if (isPuzzleComplete) {
                                        onNavigateBack() // Leave instantly
                                    } else {
                                        showExitDialog = true // Show the warning
                                    }
                                },
                            )
                        },
                        bottomBar = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // The Clue Display
                                ClueBanner(
                                    clueMap = viewModel.clueMap.collectAsState(),
                                    clueNumber = viewModel.activeWordId, // e.g., "5"
                                    onNextClick = {
                                        viewModel.nextClue()
                                    },
                                    onPreviousClick = {
                                        viewModel.previousClue()
                                    },
                                    result = viewModel.puzzleResult,
                                )

                                Spacer(modifier = Modifier.height(8.dp)) // A little breathing room before the keyboard

                                GridKeyboard(
                                    onKeyPress = {
                                        viewModel.onKeyPress(it)
                                    },
                                    onToggleDirection = {
                                        viewModel.toggleDirection()
                                    },
                                    onBackspacePress = {
                                        viewModel.onBackPress()
                                    },
                                )
                            }
                        },
                    ) {
                        Box(modifier = Modifier.padding(it)) {
                            DynamicCrosswordGrid(
                                cellMap = viewModel.cellMap,
                                selectedCellId = viewModel.selectedCellKey,
                                activeWordId = viewModel.activeWordId,
                                isPuzzleComplete = viewModel.isPuzzleComplete.collectAsState(),
                                onCellClick = {
                                    viewModel.onCellSelected("${it.row}-${it.col}")
                                },
                            )
                        }

                        val elapsedTimerText by viewModel.elapsedTimerText.collectAsState()

                        // Show Dialog when puzzle is complete
                        val resultState by viewModel.puzzleResult.collectAsState()

                        val showResultDialog by viewModel.showCompletionDialog.collectAsState()

                        // Only show the dialog if we have a generated result state
                        if (showResultDialog) {
                            resultState?.let { state ->
                                PuzzleCompleteDialog(
                                    timeTaken = elapsedTimerText,
                                    resultState = state,
                                    onDismiss = {
                                        viewModel.dismissCompletionDialog()
                                    },
                                )
                                LaunchedEffect(resultState) {
                                    GlobalOverlayController.triggerConfetti()
                                }
                            }
                        }

                        // 3. The Dialog UI
                        if (showExitDialog) {
                            AlertDialog(
                                onDismissRequest = { showExitDialog = false },
                                title = {
                                    Text(
                                        text = "Leave Puzzle?",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                    )
                                },
                                text = {
                                    Text(
                                        text = "Your progress will be saved safely. Are you sure you want to exit?",
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            showExitDialog = false
                                            onNavigateBack() // Actually trigger the navigation
                                        },
                                    ) {
                                        Text("Leave", color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showExitDialog = false }) {
                                        Text("Stay", fontWeight = FontWeight.Bold)
                                    }
                                },
                            )
                        }

                        GlobalConfettiOverlay()
                    }
                }
            }
    }

    fun onNavigateBack() {
        finish()
    }
}
