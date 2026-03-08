package com.pramod.games.crossword.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
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
import kotlinx.coroutines.delay

@Composable
internal fun CrosswordCellView(
    cell: CrosswordCell,
    cellSizePx: Int,
    selectedCellId: State<String?>,
    activeWordId: State<Int?>,
    isPuzzleComplete: State<Boolean>,
    onCellClick: () -> Unit,
) {
    val cellId = "${cell.row}-${cell.col}"

    val isFocused by remember(cellId) {
        derivedStateOf { selectedCellId.value == cellId }
    }

    val isWordHighlighted by remember(cell.acrossWordId, cell.downWordId) {
        derivedStateOf {
            val active = activeWordId.value
            active != null && (active == cell.acrossWordId || active == cell.downWordId)
        }
    }

    // ✅ OPTIMIZATION 1: Removed unnecessary `derivedStateOf` wrapper
    val puzzleComplete = isPuzzleComplete.value

    val colorScheme = MaterialTheme.colorScheme
    val isDark = remember(colorScheme.surface) { colorScheme.surface.luminance() < 0.5f }

    // ── Target Background Colors ──────────────────────────────────────────────
    val targetBackgroundColor = when {
        cell.state == CellState.NO_WORD -> Color.Transparent

        puzzleComplete && isWordHighlighted -> if (isDark) Color(0xFF1B5E20) else Color(0xFFC8E6C9)

        // ✅ VISIBILITY FIX: Solid, bold Primary color makes the cursor impossible to miss
        isFocused && !puzzleComplete -> colorScheme.primary

        // ✅ VISIBILITY FIX: PrimaryContainer creates a strong, obvious highlight trail
        isWordHighlighted -> colorScheme.primaryContainer

        cell.state == CellState.REVEALED -> colorScheme.tertiaryContainer
        else -> colorScheme.surfaceVariant
    }

    // ── Target Text Colors ────────────────────────────────────────────────────
    val targetTextColor = when {
        // ✅ VISIBILITY FIX: Focus overrides everything else so text is ALWAYS legible on the Primary background
        isFocused && !puzzleComplete -> colorScheme.onPrimary

        // Checked: Explicitly Correct (Material Green)
        cell.state == CellState.CHECKED && cell.isCheckedLetterCorrect == true -> {
            if (isDark) Color(0xFFA5D6A7) else Color(0xFF2E7D32)
        }

        // Checked: Explicitly Wrong (Material Error)
        cell.state == CellState.CHECKED && cell.isCheckedLetterCorrect == false -> colorScheme.error

        // Revealed Hint
        cell.state == CellState.REVEALED -> colorScheme.onTertiaryContainer

        // Success / Complete Highlight
        puzzleComplete && isWordHighlighted -> if (isDark) Color(0xFFA5D6A7) else Color(0xFF2E7D32)

        // Secondary Highlight Text
        isWordHighlighted -> colorScheme.onPrimaryContainer

        // Default text
        else -> colorScheme.onSurfaceVariant
    }

    val borderColor = colorScheme.outlineVariant

    val backgroundColor by animateColorAsState(targetValue = targetBackgroundColor, tween(100), label = "bg")
    val textColor by animateColorAsState(targetValue = targetTextColor, tween(100), label = "text")

    val cellScale by animateFloatAsState(
        targetValue = if (isFocused && !puzzleComplete) 1.08f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "scale"
    )

    var prevInput by remember { mutableStateOf(cell.userInput) }
    var letterScale by remember { mutableFloatStateOf(1f) }
    val animatedLetterScale by animateFloatAsState(
        targetValue = letterScale,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh), label = "pop"
    )

    LaunchedEffect(cell.userInput) {
        if (cell.userInput != prevInput && cell.userInput.isNotEmpty()) {
            letterScale = 1.4f
            delay(16)
            letterScale = 1f
        }
        prevInput = cell.userInput
    }

    val density = LocalDensity.current
    val letterFontSize = remember(cellSizePx, density) { with(density) { (cellSizePx * 0.5f).toSp() } }
    val cellPadding = remember(cellSizePx, density) { with(density) { (cellSizePx * 0.05f).toDp() } }

    val serialScale = remember(cell.serialNumber) {
        val len = cell.serialNumber?.length ?: 1
        when { len <= 1 -> 0.24f; len == 2 -> 0.16f; else -> 0.12f }
    }
    val serialFontSize = remember(cellSizePx, density, serialScale) { with(density) { (cellSizePx * serialScale).toSp() } }
    val iconBaseSizeDp = remember(cellSizePx, density) { with(density) { (cellSizePx * 0.24f).toDp() } }

    val cellShape = remember { RoundedCornerShape(2.dp) }
    val errorColor = MaterialTheme.colorScheme.error

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .then(
                if (cell.state == CellState.NO_WORD) Modifier else Modifier
                    .padding(1.dp)
                    .graphicsLayer { scaleX = cellScale; scaleY = cellScale }
                    .background(backgroundColor, cellShape)
                    .border(0.5.dp, borderColor, cellShape)
                    .clip(cellShape)
                    // ✅ OPTIMIZATION 2: Changed key from `cell.state` to `cellId`
                    .pointerInput(cellId) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            val up = waitForUpOrCancellation()
                            if (up != null && !down.isConsumed) onCellClick()
                        }
                    }
                    .drawWithContent {
                        drawContent()
                        if (cell.state == CellState.CHECKED && cell.isCheckedLetterCorrect == false) {
                            drawLine(
                                color = errorColor.copy(alpha = 0.8f),
                                start = Offset(0f, size.height),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
            ),
        contentAlignment = Alignment.Center,
    ) {
        cell.serialNumber?.let { number ->
            Text(
                text = number,
                style = TextStyle(
                    fontSize = serialFontSize,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(cellPadding),
            )
        }

        if (cell.state == CellState.REVEALED) {
            val maxZoom = 3f
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(cellPadding)
                    .size(iconBaseSizeDp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Visibility,
                    contentDescription = "Letter Revealed",
                    tint = textColor,
                    modifier = Modifier
                        .requiredSize(iconBaseSizeDp * maxZoom)
                        .graphicsLayer { scaleX = 1f / maxZoom; scaleY = 1f / maxZoom }
                )
            }
        } else if (cell.state == CellState.CHECKED && cell.isCheckedLetterCorrect == true) {
            val maxZoom = 3f
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(cellPadding)
                    .size(iconBaseSizeDp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircleOutline,
                    contentDescription = "Checked Correct",
                    tint = targetTextColor,
                    modifier = Modifier
                        .requiredSize(iconBaseSizeDp * maxZoom)
                        .graphicsLayer { scaleX = 1f / maxZoom; scaleY = 1f / maxZoom }
                )
            }
        }

        if (cell.state != CellState.NO_WORD) {
            Text(
                text = cell.userInput.uppercase(),
                style = TextStyle(
                    fontSize = letterFontSize,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                ),
                modifier = Modifier.graphicsLayer {
                    scaleX = animatedLetterScale; scaleY = animatedLetterScale
                },
            )
        }
    }
}