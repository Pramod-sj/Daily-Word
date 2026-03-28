package com.pramod.games.crossword.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pramod.games.crossword.CrosswordClue
import com.pramod.games.crossword.PuzzleResultUiState
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun ClueBanner(
    clueMap: SnapshotStateMap<Int, CrosswordClue>,
    clueNumber: State<Int?>,
    result: StateFlow<PuzzleResultUiState?>,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onViewWord: (wordId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val clue = clueMap[clueNumber.value]
    val numberText = clueNumber.value?.toString() ?: ""

    val result by result.collectAsState()

    if (clue == null) return

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onViewWord(clue.wordIdDate) },
        shape = RoundedCornerShape(16.dp), // Matches the Timer pill's 16.dp shape
        color = MaterialTheme.colorScheme.secondaryContainer, // Ties the color to the Timer
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            // Added slight vertical padding inside the surface to let the text breathe
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
        ) {
            // 1. Previous Arrow
            IconButton(onClick = onPreviousClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = "Previous Clue",
                    tint = MaterialTheme.colorScheme.primary, // Matches your Lightbulb icon
                )
            }

            if (result != null) {
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .wrapContentHeight()
                            .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "$numberText. ${clue?.answer}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = clue?.clueText ?: "No clue provided",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer, // Matches the Timer text color
                    )
                }
            } else {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = "($numberText)",
                        // ✅ Switched to standard Material typography
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        text = clue?.clueText ?: "No clue provided",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer, // Matches the Timer text color
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // 3. Next Arrow
            IconButton(onClick = onNextClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "Next Clue",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
