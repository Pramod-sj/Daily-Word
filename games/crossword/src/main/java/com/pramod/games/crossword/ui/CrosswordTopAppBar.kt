package com.pramod.games.crossword.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Spellcheck
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pramod.games.crossword.CrosswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CrosswordTopAppBar(
    viewModel: CrosswordViewModel,
    onBackClick: () -> Unit,
) {
    val elapsedTimerText by viewModel.elapsedTimerText.collectAsState()
    val result by viewModel.puzzleResult.collectAsState()

    // States to manage the Dropdown Menu visibilities
    var showRevealMenu by remember { mutableStateOf(false) }
    var showCheckMenu by remember { mutableStateOf(false) } // ✅ Added state for Check Menu

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Navigate back",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        title = {
            Text(
                text = "Crossword",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
            )
        },
        actions = {
            // 1. The Timer
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.padding(end = 8.dp),
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

            if (result == null) {
                // 2. The Check Menu (NEW)
                Box {
                    IconButton(onClick = { showCheckMenu = true }) {
                        Icon(
                            imageVector = Icons.Rounded.FactCheck, // Or Icons.Rounded.CheckCircle
                            contentDescription = "Check Options",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }

                    DropdownMenu(
                        expanded = showCheckMenu,
                        onDismissRequest = { showCheckMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                    ) {
                        DropdownMenuItem(
                            text = { Text("Check Letter") },
                            onClick = {
                                viewModel.checkLetter() // Ensure this exists in ViewModel
                                showCheckMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Rounded.Title, contentDescription = null)
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Check Word") },
                            onClick = {
                                viewModel.checkWord() // Ensure this exists in ViewModel
                                showCheckMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Rounded.Spellcheck, contentDescription = null)
                            },
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Check Entire Puzzle") },
                            onClick = {
                                viewModel.checkPuzzle() // Ensure this exists in ViewModel
                                showCheckMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Rounded.FactCheck, contentDescription = null)
                            },
                        )
                    }
                }

                // 3. The Reveal Menu (EXISTING)
                Box {
                    IconButton(onClick = { showRevealMenu = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Lightbulb,
                            contentDescription = "Reveal Options",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }

                    DropdownMenu(
                        expanded = showRevealMenu,
                        onDismissRequest = { showRevealMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                    ) {
                        DropdownMenuItem(
                            text = { Text("Reveal Letter") },
                            onClick = {
                                viewModel.revealLetter()
                                showRevealMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Rounded.Title, contentDescription = null)
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Reveal Word") },
                            onClick = {
                                viewModel.revealWord()
                                showRevealMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Rounded.Spellcheck, contentDescription = null)
                            },
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Reveal Entire Puzzle",
                                    color = MaterialTheme.colorScheme.error,
                                )
                            },
                            onClick = {
                                viewModel.revealPuzzle()
                                showRevealMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Visibility,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            },
                        )
                    }
                }
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
            ),
    )
}
