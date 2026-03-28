package com.pramod.games.crossword.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pramod.dailyword.games.common.game_rules.ui.GameScaffold
import com.pramod.games.crossword.CrosswordViewModel
import com.pramod.games.crossword.GlobalConfettiOverlay
import com.pramod.games.crossword.GlobalOverlayController
import com.pramod.games.crossword.GridKeyboard
import com.pramod.games.crossword.PuzzleCompleteDialog

@Composable
internal fun CrosswordScreen(
    viewModel: CrosswordViewModel,
    onNavigateBack: () -> Unit,
    onViewWord: (String) -> Unit
) {
    // --- State Collection ---
    val isLoading by viewModel.isLoading.collectAsState()
    val resultState by viewModel.puzzleResult.collectAsState()
    val showResultDialog by viewModel.showCompletionDialog.collectAsState()
    val elapsedTimerText by viewModel.elapsedTimerText.collectAsState()
    val isPuzzleComplete = resultState != null

    var showExitDialog by remember { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }

    val rules by viewModel.rules.collectAsStateWithLifecycle()
    val isFirstTimePlayer by viewModel.isFirstTimePlayer.collectAsStateWithLifecycle()

    // --- Side Effects & Overlays ---
    BackHandler(enabled = !isPuzzleComplete) {
        showExitDialog = true
    }

    LaunchedEffect(Unit) {
        viewModel.puzzleMessage.collect { message ->
            val result = snackBarHostState.showSnackbar(
                message = message.message,
                actionLabel = message.action?.name,
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) {
                message.action?.callback?.invoke()
            }
        }
    }

    LaunchedEffect(resultState) {
        if (resultState != null) {
            GlobalOverlayController.triggerConfetti()
        }
    }

    // --- Main Layout ---

    GameScaffold(
        title = {
            Text(
                text = "Crossword",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
            )
        },
        rules = rules,
        isFirstTime = isFirstTimePlayer,
        onMarkRulesSeen = {
            viewModel.markRulesAsSeen()
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        additionalActions = {
            CrosswordTopBarActions(viewModel)
        },
        onBackClick = {
            if (isPuzzleComplete) {
                onNavigateBack()
            } else {
                showExitDialog = true
            }
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                ClueBanner(
                    clueMap = viewModel.clueMap,
                    clueNumber = viewModel.activeWordId,
                    onNextClick = viewModel::nextClue,
                    onPreviousClick = viewModel::previousClue,
                    onViewWord = onViewWord,
                    result = viewModel.puzzleResult,
                )

                Spacer(modifier = Modifier.height(8.dp))

                GridKeyboard(
                    onKeyPress = viewModel::onKeyPress,
                    onToggleDirection = viewModel::toggleDirection,
                    onBackspacePress = viewModel::onBackPress,
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else {


                AnimatedVisibility(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .zIndex(2f),
                    visible = elapsedTimerText != "00:00",
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .padding(8.dp)
                            .zIndex(2f),
                        shadowElevation = 12.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = "Timer",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = elapsedTimerText,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                    }

                }

                DynamicCrosswordGrid(
                    cellMap = viewModel.cellMap,
                    selectedCellId = viewModel.selectedCellKey,
                    activeWordId = viewModel.activeWordId,
                    isPuzzleComplete = viewModel.isPuzzleComplete.collectAsState(),
                    onCellClick = { cell -> viewModel.onCellSelected(cell.key) },
                )
            }
        }
    }

    /*Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        topBar = {
            CrosswordTopAppBar(
                viewModel = viewModel,
                onBackClick = {
                    if (isPuzzleComplete) {
                        onNavigateBack()
                    } else {
                        showExitDialog = true
                    }
                },
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                ClueBanner(
                    clueMap = viewModel.clueMap,
                    clueNumber = viewModel.activeWordId,
                    onNextClick = viewModel::nextClue,
                    onPreviousClick = viewModel::previousClue,
                    onViewWord = onViewWord,
                    result = viewModel.puzzleResult,
                )

                Spacer(modifier = Modifier.height(8.dp))

                GridKeyboard(
                    onKeyPress = viewModel::onKeyPress,
                    onToggleDirection = viewModel::toggleDirection,
                    onBackspacePress = viewModel::onBackPress,
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else {
                DynamicCrosswordGrid(
                    cellMap = viewModel.cellMap,
                    selectedCellId = viewModel.selectedCellKey,
                    activeWordId = viewModel.activeWordId,
                    isPuzzleComplete = viewModel.isPuzzleComplete.collectAsState(),
                    onCellClick = { cell -> viewModel.onCellSelected(cell.key) },
                )
            }
        }
    }*/

    // --- Dialogs (Rendered strictly above the Scaffold) ---
    if (showResultDialog && resultState != null) {
        PuzzleCompleteDialog(
            timeTaken = elapsedTimerText,
            resultState = resultState!!,
            onDismiss = viewModel::dismissCompletionDialog,
        )
    }

    if (showExitDialog) {
        ExitWarningDialog(
            onConfirmExit = {
                showExitDialog = false
                onNavigateBack()
            },
            onDismiss = { showExitDialog = false }
        )
    }

    GlobalConfettiOverlay()
}