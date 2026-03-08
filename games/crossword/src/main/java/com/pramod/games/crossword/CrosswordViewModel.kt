package com.pramod.games.crossword

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.pramod.dialyword.router.AppRouter
import com.pramod.games.crossword.network.CrosswordRepository
import com.pramod.games.crossword.ui.boardGenerator.CellState
import com.pramod.games.crossword.ui.boardGenerator.CrosswordMapGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import kotlin.Int
import kotlin.String
import kotlin.math.roundToInt

@HiltViewModel
internal class CrosswordViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val crosswordRepository: CrosswordRepository,
    private val cellProcessor: CrosswordMapGenerator,
    private val appRouter: AppRouter
) : ViewModel() {

    // region 1. Constants & Persistence Keys
    companion object {
        private const val KEY_ELAPSED_SECONDS = "elapsed_seconds"
        private const val KEY_USER_ANSWERS = "user_answers"
        private const val KEY_REVEALED_CELLS = "revealed_cells"
        private const val KEY_PUZZLE_COMPLETE = "puzzle_complete"
    }
    // endregion


    private val crosswordId = savedStateHandle.get<String>(CrosswordActivity.EXTRA_CROSSWORD_ID)

    // region 2. SavedStateHandle Getters & Setters
    private val savedStateUserAnswers: Map<String, String>
        get() = savedStateHandle.get<Map<String, String>>(KEY_USER_ANSWERS).orEmpty()

    private val savedStateRevealedAnswerSet: Set<String>
        get() = savedStateHandle.get<Set<String>>(KEY_REVEALED_CELLS).orEmpty()

    private var savedStatePuzzleComplete: Boolean
        get() = savedStateHandle.get<Boolean>(KEY_PUZZLE_COMPLETE) ?: false
        set(value) {
            savedStateHandle[KEY_PUZZLE_COMPLETE] = value
        }

    private var elapsedSeconds: Long
        get() = savedStateHandle[KEY_ELAPSED_SECONDS] ?: 0
        set(value) {
            savedStateHandle[KEY_ELAPSED_SECONDS] = value
        }

    /**
     * Updates a single cell's answer in the persistent storage.
     */
    private fun updateUserAnswer(
        cellId: String,
        text: String,
    ) {
        val currentAnswers =
            savedStateHandle.get<Map<String, String>>(KEY_USER_ANSWERS)?.toMutableMap()
                ?: mutableMapOf()

        currentAnswers[cellId] = text
        savedStateHandle[KEY_USER_ANSWERS] = currentAnswers
    }
    // endregion

    // region 3. Observable UI State (Flows)
    val cellMap = MutableStateFlow<Map<String, CrosswordCell>>(emptyMap())
    val clueMap = MutableStateFlow<Map<Int, CrosswordClue>>(emptyMap())
    val elapsedTimerText = MutableStateFlow(formatMillis(elapsedSeconds))

    val isPuzzleComplete = MutableStateFlow(false)

    private val _puzzleResult = MutableStateFlow<PuzzleResultUiState?>(null)
    val puzzleResult = _puzzleResult.asStateFlow()

    private val _showCompletionDialog = MutableStateFlow(false)
    val showCompletionDialog = _showCompletionDialog.asStateFlow()

    // Used to send one-time messages to the UI (like a Snackbar)
    private val _puzzleMessage = MutableSharedFlow<SnackBarMessage>(extraBufferCapacity = 1)
    val puzzleMessage = _puzzleMessage.asSharedFlow()
    // endregion

    // region 4. Mutable UI Selection State
    var selectedCellKey = mutableStateOf<String?>(null)
    var activeWordId = mutableStateOf<Int?>(null)
    var isAcrossMode = mutableStateOf(true)
    private var timerJob: Job? = null

    // Tracks if we already warned them so we don't spam the UI
    private var hasShownBoardFullWarning = false
    // endregion

    // region 5. Initialization
    init {
        fetchPuzzle()
    }

    private fun fetchPuzzle() {
        viewModelScope.launch {
            crosswordId ?: return@launch
            val resource = crosswordRepository.getCrossword(crosswordId)
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.let { response ->
                        val crosswordPuzzleData = cellProcessor.generate(response = response)
                        clueMap.value = crosswordPuzzleData.clueMap

                        val restoredCells =
                            restoreCellStates(generatedMap = crosswordPuzzleData.cellMap)

                        cellMap.value = restoredCells

                        if (savedStateUserAnswers.isNotEmpty()) {
                            // if user had done some selection previous just select last cell and start timer
                            cellMap.value.entries.lastOrNull { it.value.userInput.isNotEmpty() }
                                ?.let {
                                    onCellSelected(it.key)
                                    startTimer()
                                }
                        } else {
                            cellMap.value.keys.firstOrNull()?.let { key ->
                                onCellSelected(key)
                            }
                        }

                        if (savedStatePuzzleComplete) {
                            evaluateScoreAndCompletion()
                        }
                    }
                }

                Status.ERROR -> {
                    println(resource.error)
                }
            }
        }
    }


    /**
     * Merges the freshly generated grid with answers and revealed states from SavedStateHandle.
     */
    private fun restoreCellStates(generatedMap: Map<String, CrosswordCell>): Map<String, CrosswordCell> {
        return generatedMap.mapValues { (id, cell) ->
            val savedInput = savedStateUserAnswers[id]
            if (savedInput != null) {
                cell.copy(
                    userInput = savedInput,
                    state = if (savedStateRevealedAnswerSet.contains(id)) CellState.REVEALED else cell.state
                )
            } else {
                cell
            }
        }
    }

    // endregion

    // region 6. User Input Actions (Keyboard)
    fun onKeyPress(letter: Char) {
        if (isPuzzleComplete.value) return

        val key = selectedCellKey.value ?: return
        val currentCell = cellMap.value[key] ?: return

        if (currentCell.state == CellState.REVEALED) {
            moveToNextCell(currentCell) // Still move the cursor forward
            if (timerJob == null) startTimer()
            return // Stop execution here so it doesn't overwrite the revealed letter
        }

        val updatedMap = cellMap.value.toMutableMap()
        updatedMap[key] = currentCell.copy(
            userInput = letter.toString(),
            state = CellState.DRAFT,
        )
        cellMap.value = updatedMap
        updateUserAnswer(key, letter.toString())

        moveToNextCell(currentCell)
        if (timerJob == null) startTimer()
        evaluateScoreAndCompletion()
    }

    fun onBackPress() {
        if (isPuzzleComplete.value) return

        val currSelectedKey = selectedCellKey.value ?: return
        val currentCell = cellMap.value[currSelectedKey] ?: return

        if (currentCell.state == CellState.REVEALED) {
            moveToPreviousCell(currentCell) // Still move the cursor backward
            return // Stop execution here so it doesn't clear the revealed letter
        }

        val updatedMap = cellMap.value.toMutableMap()
        updatedMap[currSelectedKey] = currentCell.copy(
            userInput = "",
            state = CellState.EMPTY,
        )
        cellMap.value = updatedMap
        updateUserAnswer(currSelectedKey, "")

        moveToPreviousCell(currentCell)
        evaluateScoreAndCompletion()
    }
    // endregion

    // region 7. Navigation & Selection Logic
    fun onCellSelected(key: String) {
        val cell = cellMap.value[key] ?: return
        val previousKey = selectedCellKey.value

        if (previousKey == key) {
            if (cell.acrossWordId != null && cell.downWordId != null) {
                isAcrossMode.value = !isAcrossMode.value
            }
        } else {
            val canStayInCurrentMode = if (isAcrossMode.value) {
                cell.acrossWordId != null
            } else {
                cell.downWordId != null
            }

            if (!canStayInCurrentMode) {
                isAcrossMode.value = !isAcrossMode.value
            }
        }

        selectedCellKey.value = key
        activeWordId.value = if (isAcrossMode.value) cell.acrossWordId else cell.downWordId
    }

    fun toggleDirection() {
        val cell = cellMap.value[selectedCellKey.value] ?: return
        if (isAcrossMode.value) {
            if (cell.downWordId != null) {
                isAcrossMode.value = false
                activeWordId.value = cell.downWordId
            }
        } else {
            if (cell.acrossWordId != null) {
                isAcrossMode.value = true
                activeWordId.value = cell.acrossWordId
            }
        }
    }

    private fun moveToNextCell(current: CrosswordCell) {
        val isAcross = isAcrossMode.value
        val wordId = activeWordId.value
        var step = 1
        var immediateNextKey: String? = null

        while (true) {
            val nextRow = if (isAcross) current.row else current.row + step
            val nextCol = if (isAcross) current.col + step else current.col
            val nextKey = "$nextRow-$nextCol"
            val nextCell = cellMap.value[nextKey]
            val sameWord =
                if (isAcross) nextCell?.acrossWordId == wordId else nextCell?.downWordId == wordId

            if (nextCell == null || !sameWord) break
            if (step == 1) immediateNextKey = nextKey
            if (nextCell.state == CellState.EMPTY) {
                selectedCellKey.value = nextKey
                return
            }
            step++
        }
        if (immediateNextKey != null) selectedCellKey.value = immediateNextKey
    }

    private fun moveToPreviousCell(current: CrosswordCell) {
        val prevRow = if (isAcrossMode.value) current.row else current.row - 1
        val prevCol = if (isAcrossMode.value) current.col - 1 else current.col
        val prevKey = "$prevRow-$prevCol"

        val prevCell = cellMap.value[prevKey]
        val sameWord = if (isAcrossMode.value) {
            prevCell?.acrossWordId == activeWordId.value
        } else {
            prevCell?.downWordId == activeWordId.value
        }

        if (prevCell != null && sameWord) {
            selectedCellKey.value = prevKey
        }
    }

    fun nextClue() {
        val sequence = getOrderedClues()
        if (sequence.isEmpty()) return
        val currentPair = Pair(activeWordId.value, isAcrossMode.value)
        val currentIndex = sequence.indexOf(currentPair)
        val nextIndex =
            if (currentIndex == -1 || currentIndex == sequence.size - 1) 0 else currentIndex + 1
        applyClueNavigation(sequence[nextIndex])
    }

    fun previousClue() {
        val sequence = getOrderedClues()
        if (sequence.isEmpty()) return
        val currentPair = Pair(activeWordId.value, isAcrossMode.value)
        val currentIndex = sequence.indexOf(currentPair)
        val prevIndex = if (currentIndex <= 0) sequence.size - 1 else currentIndex - 1
        applyClueNavigation(sequence[prevIndex])
    }

    private fun applyClueNavigation(target: Pair<Int, Boolean>) {
        val (targetWordId, targetIsAcross) = target
        activeWordId.value = targetWordId
        isAcrossMode.value = targetIsAcross

        val wordCells = cellMap.value.values.filter { cell ->
            if (targetIsAcross) cell.acrossWordId == targetWordId else cell.downWordId == targetWordId
        }
        val startingCell = wordCells.minWithOrNull(compareBy({ it.row }, { it.col }))
        startingCell?.let { selectedCellKey.value = "${it.row}-${it.col}" }
    }

    private fun getOrderedClues(): List<Pair<Int, Boolean>> {
        val cells = cellMap.value.values
        val across = cells.mapNotNull { it.acrossWordId }.distinct().sorted().map { it to true }
        val down = cells.mapNotNull { it.downWordId }.distinct().sorted().map { it to false }
        return (across + down).sortedBy { it.first }
    }
    // endregion

    // region 8. Reveal Features
    fun revealLetter() {
        val key = selectedCellKey.value ?: return
        val cell = cellMap.value[key] ?: return

        val updatedMap = cellMap.value.toMutableMap()
        updatedMap[key] = cell.copy(
            userInput = cell.correctChar.toString(),
            state = CellState.REVEALED,
        )
        cellMap.value = updatedMap

        updateUserAnswer(key, cell.correctChar.toString())
        savedStateHandle[KEY_REVEALED_CELLS] =
            savedStateRevealedAnswerSet.toMutableSet().apply { add(key) }
        evaluateScoreAndCompletion()
    }

    fun revealWord() {
        val wordId = activeWordId.value ?: return
        val isAcross = isAcrossMode.value
        val updatedMap = cellMap.value.toMutableMap()

        updatedMap.forEach { (key, cell) ->
            val belongsToWord =
                if (isAcross) cell.acrossWordId == wordId else cell.downWordId == wordId
            if (belongsToWord) {
                updatedMap[key] =
                    cell.copy(userInput = cell.correctChar.toString(), state = CellState.REVEALED)
                updateUserAnswer(key, cell.correctChar.toString())
                savedStateHandle[KEY_REVEALED_CELLS] =
                    savedStateRevealedAnswerSet.toMutableSet().apply { add(key) }
            }
        }
        cellMap.value = updatedMap
        evaluateScoreAndCompletion()
    }

    fun revealPuzzle() {
        val updatedMap = cellMap.value.toMutableMap()
        updatedMap.forEach { (key, cell) ->
            if (cell.state != CellState.NO_WORD && !cell.userInput.equals(
                    cell.correctChar.toString(),
                    ignoreCase = true,
                )
            ) {
                updatedMap[key] =
                    cell.copy(userInput = cell.correctChar.toString(), state = CellState.REVEALED)
                updateUserAnswer(key, cell.correctChar.toString())
                savedStateHandle[KEY_REVEALED_CELLS] =
                    savedStateRevealedAnswerSet.toMutableSet().apply { add(key) }
            }
        }
        cellMap.value = updatedMap
        evaluateScoreAndCompletion()
    }
    // endregion

    // region 9. Check Features
    fun checkLetter() {
        val key = selectedCellKey.value ?: return
        val cell = cellMap.value[key] ?: return

        // Don't check if empty or already revealed
        if (cell.userInput.isEmpty() || cell.state == CellState.REVEALED) return

        val updatedMap = cellMap.value.toMutableMap()
        updatedMap[key] = cell.copy(
            state = CellState.CHECKED,
            isCheckedLetterCorrect = cell.correctChar.toString()
                .equals(cell.userInput, ignoreCase = true)
        )
        cellMap.value = updatedMap
    }

    fun checkWord() {
        val wordId = activeWordId.value ?: return
        val isAcross = isAcrossMode.value
        val updatedMap = cellMap.value.toMutableMap()

        updatedMap
            .filter { it.value.state != CellState.NO_WORD }
            .forEach { (key, cell) ->
                val belongsToWord =
                    if (isAcross) cell.acrossWordId == wordId else cell.downWordId == wordId

                // Only check cells in the word that have user input and aren't already revealed
                if (belongsToWord && cell.userInput.isNotEmpty() && cell.state != CellState.REVEALED) {
                    updatedMap[key] = cell.copy(
                        state = CellState.CHECKED,
                        isCheckedLetterCorrect = cell.correctChar.toString()
                            .equals(cell.userInput, ignoreCase = true)
                    )
                }
            }
        cellMap.value = updatedMap
    }

    fun checkPuzzle() {
        val updatedMap = cellMap.value.toMutableMap()

        updatedMap
            .filter { it.value.state != CellState.NO_WORD }
            .forEach { (key, cell) ->
                // Only check cells that have user input and aren't already revealed
                if (cell.userInput.isNotEmpty() && cell.state != CellState.REVEALED) {
                    updatedMap[key] = cell.copy(
                        state = CellState.CHECKED,
                        isCheckedLetterCorrect = cell.correctChar.toString()
                            .equals(cell.userInput, ignoreCase = true)
                    )
                }
            }
        cellMap.value = updatedMap
    }

    // endregion

    // region 9. Scoring & Completion Logic
    private fun evaluateScoreAndCompletion() {
        val cells = cellMap.value.values.filter { it.state != CellState.NO_WORD }
        val acrossWords = cells.mapNotNull { it.acrossWordId }.distinct()
        val downWords = cells.mapNotNull { it.downWordId }.distinct()
        val totalWords = acrossWords.size + downWords.size

        if (totalWords == 0) return

        var perfectWordsCount = 0
        var filledCellsCount = 0

        cells.forEach { if (it.userInput.isNotEmpty()) filledCellsCount++ }
        val isBoardFull = filledCellsCount == cells.size

        if (!isBoardFull) {
            hasShownBoardFullWarning = false
        }

        acrossWords.forEach { wordId ->
            val wordCells = cells.filter { it.acrossWordId == wordId }
            val isFilled = wordCells.all { it.userInput.isNotEmpty() }
            val isCorrect =
                wordCells.all { it.userInput.equals(it.correctChar.toString(), ignoreCase = true) }
            if (isFilled && isCorrect && wordCells.none { it.state == CellState.REVEALED }) perfectWordsCount++
        }

        downWords.forEach { wordId ->
            val wordCells = cells.filter { it.downWordId == wordId }
            val isFilled = wordCells.all { it.userInput.isNotEmpty() }
            val isCorrect =
                wordCells.all { it.userInput.equals(it.correctChar.toString(), ignoreCase = true) }
            if (isFilled && isCorrect && wordCells.none { it.state == CellState.REVEALED }) perfectWordsCount++
        }

        val calculatedScore = ((perfectWordsCount.toFloat() / totalWords) * 100).roundToInt()

        val allCorrect =
            cells.all { it.userInput.equals(it.correctChar.toString(), ignoreCase = true) }

        if (isBoardFull) {
            if (allCorrect) {
                // VICTORY!
                isPuzzleComplete.value = true
                stopTimer()
                onPuzzleCompleted(generateResultState(calculatedScore))
            } else {
                if (!hasShownBoardFullWarning) {
                    hasShownBoardFullWarning = true
                    // FULL BUT WRONG!
                    // Alert the user so they aren't confused
                    _puzzleMessage.tryEmit(
                        value = SnackBarMessage(
                            message = "So close! Want to spot the mistakes?",
                            action = Action("Check puzzle") {
                                checkPuzzle()
                            }
                        )
                    )
                }
            }
        }

    }

    private fun onPuzzleCompleted(state: PuzzleResultUiState) {
        _puzzleResult.value = state
        _showCompletionDialog.value = true
        savedStatePuzzleComplete = true
    }

    fun dismissCompletionDialog() {
        _showCompletionDialog.value = false
    }

    private fun generateResultState(score: Int): PuzzleResultUiState = when {
        score >= 99 -> {
            PuzzleResultUiState(
                title = "Flawless!",
                message = "Absolute perfection!",
                buttonText = "Incredible",
                tier = ScoreTier.EXCELLENT,
                score = score
            )
        }

        score >= 90 -> {
            PuzzleResultUiState(
                title = "Amazing!",
                message = "So close to perfect!",
                buttonText = "Awesome",
                tier = ScoreTier.EXCELLENT,
                score = score
            )
        }

        score >= 80 -> {
            PuzzleResultUiState(
                "Outstanding!", "Brilliant job!", "Awesome", ScoreTier.EXCELLENT, score = score
            )
        }

        score >= 50 -> {
            PuzzleResultUiState(
                title = "Good Work!",
                message = "A solid effort.",
                buttonText = "Awesome",
                tier = ScoreTier.GOOD,
                score = score
            )
        }

        score > 0 -> {
            PuzzleResultUiState(
                title = "Nice Try!",
                message = "You're getting there!",
                buttonText = "Awesome",
                tier = ScoreTier.FAIR,
                score = score
            )
        }

        else -> {
            PuzzleResultUiState(
                title = "Keep Learning!",
                message = "Review these revealed words.",
                buttonText = "Got it",
                tier = ScoreTier.LEARNING,
                score = score
            )
        }
    }
    // endregion

    // region 10. Timer & Formatting Helpers
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            withContext(Dispatchers.Default) {
                while (true) {
                    delay(1000L)
                    elapsedSeconds += 1000L
                    elapsedTimerText.value = formatMillis(elapsedSeconds)
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    private fun formatMillis(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
    // endregion
}

data class CrosswordCell(
    val row: Int,
    val col: Int,
    val serialNumber: String? = null,
    // Explicitly link to the Word IDs from your JSON
    val acrossWordId: Int? = null,
    val downWordId: Int? = null,
    val correctChar: Char? = null,
    val userInput: String = "",
    val state: CellState = CellState.EMPTY,
    val isCheckedLetterCorrect: Boolean? = null
)

// ✅ Separate model to hold clues — don't put them in the cell
data class CrosswordClue(
    val wordId: Int,
    val clueText: String,
    val direction: String, // "across" or "down"
    val answer: String, // optional, useful for hint feature
    val startCellKey: String,
    val wordIdDate: String,
)

enum class ScoreTier {
    EXCELLENT, GOOD, FAIR, LEARNING,
}

data class PuzzleResultUiState(
    val title: String,
    val message: String,
    val buttonText: String,
    val tier: ScoreTier,
    val score: Int,
)


data class SnackBarMessage(
    val message: String,
    val duration: Int = Snackbar.LENGTH_SHORT,
    val animation: Int = Snackbar.ANIMATION_MODE_SLIDE,
    val action: Action? = null,
    val parentViewId: Int? = null,
    val anchorId: Int? = null
)


data class Action(
    val name: String? = null,
    val callback: (() -> Unit)? = null
)