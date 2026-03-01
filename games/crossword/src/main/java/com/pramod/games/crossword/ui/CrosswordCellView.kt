package com.pramod.games.crossword.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pramod.games.crossword.CrosswordCell
import com.pramod.games.crossword.ui.boardGenerator.CellState

@Composable
fun CrosswordCellView(
    cell: CrosswordCell,
    cellSizePx: Int,
    selectedCellId: State<String?>,
    activeWordId: State<Int?>,
    isPuzzleComplete: State<Boolean>,
    onCellClick: () -> Unit,
) {
    val isFocused = selectedCellId.value == "${cell.row}-${cell.col}"
    val isWordHighlighted =
        activeWordId.value == cell.acrossWordId || activeWordId.value == cell.downWordId

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val isPuzzleComplete = isPuzzleComplete.value

    val backgroundColor =
        when {
            cell.state == CellState.NO_WORD -> Color.Transparent

            // REVIEW MODE: When finished, highlight the entire word uniformly
            isPuzzleComplete && isWordHighlighted -> MaterialTheme.colorScheme.tertiaryContainer

            // GAMEPLAY MODE: Active typing cursor (Yellow)
            isFocused && !isPuzzleComplete -> if (isDark) Color(0xFFFBC02D) else Color(0xFFFFD54F)

            // GAMEPLAY MODE: Active word highlight (Blue)
            isWordHighlighted -> MaterialTheme.colorScheme.secondaryContainer

            // Hinted/Revealed cells (Disabled look)
            cell.state == CellState.REVEALED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)

            // Normal empty cells
            else -> MaterialTheme.colorScheme.surfaceColorAtElevation(0.2.dp)
        }

    val textColor =
        when {
            // REVIEW MODE: Match the text to the tertiary container
            isPuzzleComplete && isWordHighlighted -> MaterialTheme.colorScheme.onTertiaryContainer

            isFocused && !isPuzzleComplete -> Color.Black

            isWordHighlighted -> MaterialTheme.colorScheme.onSecondaryContainer

            cell.state == CellState.REVEALED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)

            else -> MaterialTheme.colorScheme.onSurface
        }

    // 3. Subtler colors for the structure so the letters stand out
    val serialColor = textColor
    val borderColor = MaterialTheme.colorScheme.outlineVariant

    val density = LocalDensity.current
    val letterFontSize = with(density) { (cellSizePx * 0.5f).toSp() }
    val cellPadding = with(density) { (cellSizePx * 0.05f).toDp() }

    val serialLength = cell.serialNumber?.length ?: 1
    val serialScale =
        when {
            serialLength <= 1 -> 0.24f

            // 1 digit: Standard size (24% of cell height)
            serialLength == 2 -> 0.16f

            // 2 digits: Shrink to 18% so it doesn't push right
            else -> 0.12f // 3+ digits: Shrink even further just in case
        }
    val serialFontSize = with(density) { (cellSizePx * serialScale).toSp() }

    // A slightly softer corner radius (2.dp) fits the modern Material 3 aesthetic better
    val cellShape = RoundedCornerShape(2.dp)

    Box(
        modifier =
            Modifier
                .aspectRatio(1f)
                .then(
                    if (cell.state == CellState.NO_WORD) {
                        Modifier
                    } else {
                        Modifier
                            .padding(1.dp) // Keeps your nice, subtle tile gap
                            // Note: Apply background BEFORE border for cleaner edge rendering
                            .background(backgroundColor, cellShape)
                            .border(0.5.dp, borderColor, cellShape)
                            .pointerInput(cell.state) {
                                if (cell.state != CellState.NO_WORD) {
                                    awaitEachGesture {
                                        val down = awaitFirstDown()
                                        val up = waitForUpOrCancellation()
                                        if (up != null && !down.isConsumed) {
                                            onCellClick()
                                        }
                                    }
                                }
                            }
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        // 1. Serial Number (Top Left)
        cell.serialNumber?.let { number ->
            Text(
                text = number,
                style =
                    TextStyle(
                        fontSize = serialFontSize,
                        fontWeight = FontWeight.Bold,
                        color = serialColor, // ✅ Subtler text color
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                    ),
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(cellPadding),
            )
        }

        if (cell.state == CellState.REVEALED) {
            val maxZoom = 3f
            val iconBaseSizeDp = with(density) { (cellSizePx * serialScale).toDp() }

            // 1. The Wrapper acts as the true layout boundary.
            // It is 1x size, and it aligns flawlessly to TopEnd.
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(cellPadding)
                        .size(iconBaseSizeDp),
                contentAlignment = Alignment.Center,
            ) {
                // 2. The Icon draws huge, but scales down visually to fit the Wrapper Box perfectly.
                Icon(
                    imageVector = Icons.Rounded.Visibility,
                    contentDescription = "Letter Revealed",
                    tint = serialColor,
                    modifier =
                        Modifier
                            .requiredSize(iconBaseSizeDp * maxZoom)
                            .graphicsLayer {
                                scaleX = 1f / maxZoom
                                scaleY = 1f / maxZoom
                            },
                )
            }
        }

        // 2. User Input Letter (Center)
        if (cell.state != CellState.NO_WORD) {
            Text(
                text = cell.userInput.uppercase(),
                style =
                    TextStyle(
                        fontSize = letterFontSize,
                        fontWeight = FontWeight.Bold, // ✅ Bold is cleaner than ExtraBold
                        color = textColor, // ✅ Dynamic, highly readable contrast color
                    ),
            )
        }
    }
}
