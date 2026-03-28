package com.pramod.dailyword.games.common.game_rules.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pramod.dailyword.games.common.game_rules.model.GameRule
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScaffold(
    title: @Composable () -> Unit,
    rules: List<GameRule>,
    isFirstTime: Boolean,
    onMarkRulesSeen: () -> Unit,
    onBackClick: () -> Unit,
    additionalActions: @Composable RowScope.() -> Unit = {}, // 1. Added the Slot!
    snackbarHost: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    var showRulesSheet by remember { mutableStateOf(false) }

    LaunchedEffect(isFirstTime) {
        if (isFirstTime) {
            delay(450)
            showRulesSheet = true
        }
    }

    Scaffold(
        snackbarHost = snackbarHost,
        topBar = {
            TopAppBar(
                title = title,
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    additionalActions()
                    IconButton(onClick = { showRulesSheet = true }) {
                        Icon(Icons.Rounded.HelpOutline, contentDescription = "How to play")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                )
            )
        },
        bottomBar = bottomBar
    ) { paddingValues ->
        content(paddingValues)
    }

    if (showRulesSheet && rules.isNotEmpty()) {
        GameInstructionsBottomSheet(
            rules = rules,
            onDismiss = {
                showRulesSheet = false
                onMarkRulesSeen()
            }
        )
    }
}