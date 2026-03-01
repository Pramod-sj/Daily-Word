package com.pramod.games.crossword.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import com.pramod.games.crossword.CrosswordCell
import com.pramod.games.crossword.ui.boardGenerator.CellState
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun DynamicCrosswordGrid(
    cellMap: StateFlow<Map<String, CrosswordCell>>,
    activeWordId: MutableState<Int?>,
    selectedCellId: MutableState<String?>,
    isPuzzleComplete: State<Boolean>,
    onCellClick: (CrosswordCell) -> Unit,
) {
    val cellMap by cellMap.collectAsState()

    val (rows, cols) = remember(cellMap) { calculateGridDimensions(cellMap) }

    if (cols == 0) return

    var cellSizePx by remember { mutableIntStateOf(0) }

    var cellFocusTarget by remember { mutableStateOf<WordFocusTarget?>(null) }

    LaunchedEffect(selectedCellId.value, cellSizePx) {
        val currentCellKey = selectedCellId.value
        if (currentCellKey != null && cellSizePx > 0) {
            // Find the exact cell from the map
            val cell = cellMap[currentCellKey]

            if (cell != null) {
                // Calculate the exact center of this single cell
                val centerX = (cell.col + 0.5f) * cellSizePx
                val centerY = (cell.row + 0.5f) * cellSizePx
                val cellDim = cellSizePx.toFloat()

                // We can reuse your existing WordFocusTarget data class!
                // Since it's just one cell, the start and center are the exact same point.
                cellFocusTarget =
                    WordFocusTarget(
                        startOffset = Offset(centerX, centerY),
                        centerOffset = Offset(centerX, centerY),
                        widthPx = cellDim,
                        heightPx = cellDim,
                    )
            }
        }
    }

    ZoomableBox(
        modifier = Modifier.fillMaxSize(),
        minZoom = 0.6f,
        maxZoom = 5f,
        focusTarget = cellFocusTarget, // ✅ Pass the single cell target
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .onSizeChanged { size ->
                        cellSizePx = size.width / cols
                    },
        ) {
            for (r in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (c in 0 until cols) {
                        val key = "$r-$c"
                        val cell = cellMap[key] ?: CrosswordCell(r, c, state = CellState.NO_WORD)

                        key(key) {
                            Box(modifier = Modifier.weight(1f)) {
                                CrosswordCellView(
                                    cell = cell,
                                    cellSizePx = cellSizePx,
                                    selectedCellId = selectedCellId,
                                    activeWordId = activeWordId,
                                    isPuzzleComplete = isPuzzleComplete,
                                    onCellClick = { onCellClick.invoke(cell) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun calculateGridDimensions(cellMap: Map<String, CrosswordCell>): Pair<Int, Int> {
    if (cellMap.isEmpty()) return Pair(0, 0)

    var maxRow = 0
    var maxCol = 0

    cellMap.values.forEach { cell ->
        if (cell.row > maxRow) maxRow = cell.row
        if (cell.col > maxCol) maxCol = cell.col
    }

    // We add +1 because indices are 0-based
    // (e.g., if max row index is 14, the grid height is 15)
    return Pair(maxRow + 1, maxCol + 1)
}
