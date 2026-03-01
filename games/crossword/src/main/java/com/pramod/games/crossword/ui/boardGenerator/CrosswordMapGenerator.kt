package com.pramod.games.crossword.ui.boardGenerator

import com.pramod.games.crossword.CrosswordCell
import com.pramod.games.crossword.CrosswordClue
import com.pramod.games.crossword.network.data.CrosswordResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class CrosswordMapGenerator
    @Inject
    constructor() {
        data class CrosswordPuzzleData(
            val cellMap: Map<String, CrosswordCell>,
            val clueMap: Map<Int, CrosswordClue>,
        )

        suspend fun generate(response: CrosswordResponse): CrosswordPuzzleData =
            withContext(Dispatchers.Default) {
                val grid = mutableMapOf<String, CrosswordCell>()
                val clues = mutableMapOf<Int, CrosswordClue>()

                response.puzzle.forEachIndexed { puzzleIndex, puzzle ->
                    val currentWordId = puzzleIndex + 1
                    var isSerialSet = false

                    puzzle.answer.toCharArray().forEachIndexed { index, char ->
                        val row: Int
                        val col: Int

                        if (puzzle.direction == "across" || puzzle.direction == "horizontal") {
                            row = puzzle.row
                            col = puzzle.col + index
                        } else {
                            row = puzzle.row + index
                            col = puzzle.col
                        }

                        val key = "$row-$col"

                        if (clues[currentWordId] == null) {
                            clues[currentWordId] =
                                CrosswordClue(
                                    wordId = currentWordId,
                                    clueText = puzzle.clue,
                                    direction = puzzle.direction,
                                    answer = puzzle.answer,
                                    startCellKey = key,
                                )
                        }

                        var existingCell = grid[key]

                        if (existingCell != null) {
                            existingCell =
                                if (puzzle.direction == "across" || puzzle.direction == "horizontal") {
                                    existingCell.copy(acrossWordId = currentWordId)
                                } else {
                                    existingCell.copy(downWordId = currentWordId)
                                }

                            if (!isSerialSet) {
                                existingCell = existingCell.copy(serialNumber = "$currentWordId")
                                isSerialSet = true
                            }

                            grid[key] = existingCell
                        } else {
                            var newCell =
                                CrosswordCell(
                                    row = row,
                                    col = col,
                                    correctChar = char,
                                    state = CellState.EMPTY,
                                    serialNumber =
                                        if (!isSerialSet) {
                                            isSerialSet = true
                                            "$currentWordId"
                                        } else {
                                            null
                                        },
                                )

                            newCell =
                                if (puzzle.direction == "across" || puzzle.direction == "horizontal") {
                                    newCell.copy(acrossWordId = currentWordId)
                                } else {
                                    newCell.copy(downWordId = currentWordId)
                                }

                            grid[key] = newCell
                        }
                    }
                }

                // Fill remaining cells with NO_WORD
                for (row in 0 until response.gridInfo.rows) {
                    for (col in 0 until response.gridInfo.cols) {
                        val cell = grid["$row-$col"]
                        if (cell == null) {
                            grid["$row-$col"] =
                                CrosswordCell(
                                    row = row,
                                    col = col,
                                    state = CellState.NO_WORD,
                                )
                        }
                    }
                }

                CrosswordPuzzleData(
                    cellMap = grid.toMap(),
                    clueMap = clues.toMap(),
                )
            }
    }

enum class CellState {
    NO_WORD, // Black square
    EMPTY, // White square, no text
    DRAFT, // Has text, but not checked
    REVEALED,
}
