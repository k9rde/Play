package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.ArcadeSoundEngine
import com.example.data.GamePreferencesRepository
import com.example.data.StoredGameStatsAndSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.random.Random

const val BOARD_COLS = 10
const val BOARD_ROWS = 20

enum class GameStatus {
    READY,
    PLAYING,
    PAUSED,
    GAME_OVER
}

enum class HapticCue {
    TAP,
    HARD_DROP,
    LINE_CLEAR,
    GAME_OVER
}

data class LockedCell(
    val type: TetrominoType,
    val hueOffset: Float
)

data class ActivePiece(
    val type: TetrominoType,
    val rotation: Int = 0,
    val x: Int = 3,
    val y: Int = 0,
    val hueOffset: Float = 0f
) {
    fun worldCells(): List<GridPoint> =
        type.cells(rotation).map { GridPoint(x + it.col, y + it.row) }
}

data class ClearBanner(
    val id: Long,
    val linesCleared: Int,
    val points: Int,
    val combo: Int
)

data class GameUiState(
    val board: List<List<LockedCell?>> = emptyBoard(),
    val activePiece: ActivePiece? = null,
    val ghostY: Int? = null,
    val holdPiece: TetrominoType? = null,
    val holdHueOffset: Float = 0f,
    val canHold: Boolean = true,
    val nextQueue: List<Pair<TetrominoType, Float>> = emptyList(),
    val clearingRows: Set<Int> = emptySet(),
    val clearBanner: ClearBanner? = null,
    val score: Int = 0,
    val highScore: Int = 0,
    val isNewRecord: Boolean = false,
    val level: Int = 1,
    val startingLevel: Int = 1,
    val lines: Int = 0,
    val combo: Int = 0,
    val status: GameStatus = GameStatus.READY,
    val statsAndSettings: StoredGameStatsAndSettings = StoredGameStatsAndSettings(),
    val showSettings: Boolean = false
)

fun emptyBoard(): List<List<LockedCell?>> =
    List(BOARD_ROWS) { List(BOARD_COLS) { null } }

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GamePreferencesRepository(application.applicationContext)
    private val soundEngine = ArcadeSoundEngine()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _hapticCues = MutableSharedFlow<HapticCue>(extraBufferCapacity = 8)
    val hapticCues: SharedFlow<HapticCue> = _hapticCues.asSharedFlow()

    private val bag = ArrayDeque<Pair<TetrominoType, Float>>()
    private var gravityJob: Job? = null
    private var lockDelayJob: Job? = null
    private var lockResetCount = 0
    private var bannerCounter = 0L
    private var currentHueCycle = 0f

    init {
        viewModelScope.launch {
            repository.stateFlow.collect { stored ->
                soundEngine.isEnabled = stored.soundEnabled
                _uiState.update { current ->
                    current.copy(
                        highScore = max(current.highScore, stored.highScore),
                        statsAndSettings = stored
                    )
                }
            }
        }
        preparePreviewQueue()
    }

    private fun refillBagIfNeeded() {
        while (bag.size < 7) {
            val shuffled = TetrominoType.entries.shuffled()
            for (type in shuffled) {
                currentHueCycle = (currentHueCycle + 28f + Random.nextFloat() * 14f) % 360f
                bag.addLast(type to currentHueCycle)
            }
        }
    }

    private fun preparePreviewQueue() {
        bag.clear()
        refillBagIfNeeded()
        val preview = (0 until 3).map { bag.elementAt(it) }
        _uiState.update { it.copy(nextQueue = preview) }
    }

    fun setStartingLevel(level: Int) {
        val clamped = level.coerceIn(1, 10)
        _uiState.update { it.copy(startingLevel = clamped) }
    }

    fun startNewGame() {
        gravityJob?.cancel()
        lockDelayJob?.cancel()
        bag.clear()
        refillBagIfNeeded()

        val startLvl = _uiState.value.startingLevel
        val first = bag.removeFirst()
        refillBagIfNeeded()
        val nextThree = (0 until 3).map { bag.elementAt(it) }

        val board = emptyBoard()
        val spawned = spawnPiece(first.first, first.second)
        val ghostY = computeGhostY(board, spawned)

        _uiState.update { state ->
            state.copy(
                board = board,
                activePiece = spawned,
                ghostY = ghostY,
                holdPiece = null,
                holdHueOffset = 0f,
                canHold = true,
                nextQueue = nextThree,
                clearingRows = emptySet(),
                clearBanner = null,
                score = 0,
                isNewRecord = false,
                level = startLvl,
                lines = 0,
                combo = 0,
                status = GameStatus.PLAYING,
                showSettings = false
            )
        }
        startGravityLoop()
    }

    fun pauseGame() {
        if (_uiState.value.status == GameStatus.PLAYING) {
            gravityJob?.cancel()
            lockDelayJob?.cancel()
            _uiState.update { it.copy(status = GameStatus.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_uiState.value.status == GameStatus.PAUSED) {
            _uiState.update { it.copy(status = GameStatus.PLAYING, showSettings = false) }
            startGravityLoop()
        }
    }

    fun togglePause() {
        when (_uiState.value.status) {
            GameStatus.PLAYING -> pauseGame()
            GameStatus.PAUSED -> resumeGame()
            else -> Unit
        }
    }

    fun openSettings() {
        val wasPlaying = _uiState.value.status == GameStatus.PLAYING
        if (wasPlaying) {
            gravityJob?.cancel()
            lockDelayJob?.cancel()
        }
        _uiState.update {
            it.copy(
                showSettings = true,
                status = if (wasPlaying) GameStatus.PAUSED else it.status
            )
        }
    }

    fun closeSettings() {
        _uiState.update { it.copy(showSettings = false) }
    }

    fun moveLeft() = tryMove(dx = -1, dy = 0, userTriggered = true)

    fun moveRight() = tryMove(dx = 1, dy = 0, userTriggered = true)

    fun softDrop(): Boolean {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING || state.clearingRows.isNotEmpty()) return false
        val moved = tryMove(dx = 0, dy = 1, userTriggered = true)
        if (moved) {
            addScore(1)
        }
        return moved
    }

    fun hardDrop() {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING || state.clearingRows.isNotEmpty()) return
        val piece = state.activePiece ?: return
        val targetY = computeGhostY(state.board, piece)
        val distance = (targetY - piece.y).coerceAtLeast(0)
        val droppedPiece = piece.copy(y = targetY)
        lockDelayJob?.cancel()

        soundEngine.playHardDrop()
        emitHaptic(HapticCue.HARD_DROP)

        _uiState.update {
            it.copy(
                activePiece = droppedPiece,
                score = it.score + distance * 2
            )
        }
        lockActivePiece()
    }

    fun rotateClockwise() = tryRotate(clockwise = true)

    fun rotateCounterClockwise() = tryRotate(clockwise = false)

    fun holdCurrentPiece() {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING || !state.canHold || state.clearingRows.isNotEmpty()) return
        val current = state.activePiece ?: return
        lockDelayJob?.cancel()

        soundEngine.playHold()
        emitHaptic(HapticCue.TAP)

        val nextActive: ActivePiece
        val updatedQueue: List<Pair<TetrominoType, Float>>
        if (state.holdPiece == null) {
            refillBagIfNeeded()
            val popped = bag.removeFirst()
            refillBagIfNeeded()
            nextActive = spawnPiece(popped.first, popped.second)
            updatedQueue = (0 until 3).map { bag.elementAt(it) }
        } else {
            nextActive = spawnPiece(state.holdPiece, state.holdHueOffset)
            updatedQueue = state.nextQueue
        }

        if (!isValidPosition(state.board, nextActive)) {
            triggerGameOver()
            return
        }

        _uiState.update {
            it.copy(
                activePiece = nextActive,
                ghostY = computeGhostY(it.board, nextActive),
                holdPiece = current.type,
                holdHueOffset = current.hueOffset,
                canHold = false,
                nextQueue = updatedQueue
            )
        }
    }

    private fun tryMove(dx: Int, dy: Int, userTriggered: Boolean): Boolean {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING || state.clearingRows.isNotEmpty()) return false
        val piece = state.activePiece ?: return false
        val candidate = piece.copy(x = piece.x + dx, y = piece.y + dy)

        if (!isValidPosition(state.board, candidate)) {
            if (dy > 0 && !userTriggered) {
                scheduleLockIfGrounded(state.board, piece)
            }
            return false
        }

        if (userTriggered && dx != 0) {
            soundEngine.playMove()
        }

        _uiState.update {
            it.copy(
                activePiece = candidate,
                ghostY = computeGhostY(it.board, candidate)
            )
        }

        if (isGrounded(state.board, candidate)) {
            if (userTriggered && lockResetCount < 12) {
                lockResetCount++
                scheduleLockIfGrounded(state.board, candidate, resetExisting = true)
            } else {
                scheduleLockIfGrounded(state.board, candidate, resetExisting = false)
            }
        } else {
            lockDelayJob?.cancel()
        }
        return true
    }

    private fun tryRotate(clockwise: Boolean) {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING || state.clearingRows.isNotEmpty()) return
        val piece = state.activePiece ?: return
        val nextRotation = ((piece.rotation + if (clockwise) 1 else -1) % 4 + 4) % 4
        val kicks = SrsWallKicks.getKicks(piece.type, piece.rotation, clockwise)

        for (kick in kicks) {
            val candidate = piece.copy(
                rotation = nextRotation,
                x = piece.x + kick.col,
                y = piece.y + kick.row
            )
            if (isValidPosition(state.board, candidate)) {
                soundEngine.playRotate()
                emitHaptic(HapticCue.TAP)

                _uiState.update {
                    it.copy(
                        activePiece = candidate,
                        ghostY = computeGhostY(it.board, candidate)
                    )
                }

                if (isGrounded(state.board, candidate)) {
                    if (lockResetCount < 12) {
                        lockResetCount++
                        scheduleLockIfGrounded(state.board, candidate, resetExisting = true)
                    }
                } else {
                    lockDelayJob?.cancel()
                }
                return
            }
        }
    }

    private fun scheduleLockIfGrounded(
        board: List<List<LockedCell?>>,
        piece: ActivePiece,
        resetExisting: Boolean = false
    ) {
        if (!isGrounded(board, piece)) return
        if (lockDelayJob?.isActive == true && !resetExisting) return
        lockDelayJob?.cancel()
        lockDelayJob = viewModelScope.launch {
            delay(430L)
            val latest = _uiState.value
            if (latest.status == GameStatus.PLAYING &&
                latest.activePiece != null &&
                isGrounded(latest.board, latest.activePiece)
            ) {
                lockActivePiece()
            }
        }
    }

    private fun lockActivePiece() {
        val state = _uiState.value
        val piece = state.activePiece ?: return
        lockDelayJob?.cancel()
        lockResetCount = 0

        val mutableBoard = state.board.map { it.toMutableList() }.toMutableList()
        for (cell in piece.worldCells()) {
            if (cell.row in 0 until BOARD_ROWS && cell.col in 0 until BOARD_COLS) {
                mutableBoard[cell.row][cell.col] = LockedCell(
                    type = piece.type,
                    hueOffset = piece.hueOffset
                )
            }
        }

        val fullRows = mutableBoard.indices.filter { rowIdx ->
            mutableBoard[rowIdx].all { it != null }
        }

        if (fullRows.isNotEmpty()) {
            val clearedCount = fullRows.size
            val newCombo = state.combo + 1
            val basePoints = when (clearedCount) {
                1 -> 100
                2 -> 300
                3 -> 500
                else -> 800
            }
            val comboBonus = if (newCombo > 1) (newCombo - 1) * 50 * state.level else 0
            val pointsEarned = basePoints * state.level + comboBonus
            val updatedLines = state.lines + clearedCount
            val updatedLevel = max(state.startingLevel, (updatedLines / 10) + state.startingLevel)
            val leveledUp = updatedLevel > state.level
            val updatedScore = state.score + pointsEarned
            val beatRecord = updatedScore > state.highScore && state.highScore > 0

            soundEngine.playLineClear(clearedCount)
            if (leveledUp) {
                viewModelScope.launch {
                    delay(140L)
                    soundEngine.playLevelUp()
                }
            }
            emitHaptic(HapticCue.LINE_CLEAR)

            bannerCounter++
            val banner = ClearBanner(
                id = bannerCounter,
                linesCleared = clearedCount,
                points = pointsEarned,
                combo = newCombo
            )

            _uiState.update {
                it.copy(
                    board = mutableBoard,
                    activePiece = null,
                    ghostY = null,
                    clearingRows = fullRows.toSet(),
                    clearBanner = banner,
                    score = updatedScore,
                    highScore = max(it.highScore, updatedScore),
                    isNewRecord = it.isNewRecord || beatRecord,
                    lines = updatedLines,
                    level = updatedLevel,
                    combo = newCombo
                )
            }

            viewModelScope.launch {
                repository.recordGameProgress(updatedScore, updatedLevel, clearedCount)
                delay(180L)
                val collapsed = mutableBoard.filterIndexed { idx, _ -> idx !in fullRows }.toMutableList()
                while (collapsed.size < BOARD_ROWS) {
                    collapsed.add(0, MutableList(BOARD_COLS) { null })
                }
                _uiState.update {
                    it.copy(
                        board = collapsed,
                        clearingRows = emptySet()
                    )
                }
                spawnNextPiece()
            }
        } else {
            val currentScore = state.score
            val beatRecord = currentScore > state.highScore && state.highScore > 0
            _uiState.update {
                it.copy(
                    board = mutableBoard,
                    activePiece = null,
                    ghostY = null,
                    combo = 0,
                    highScore = max(it.highScore, currentScore),
                    isNewRecord = it.isNewRecord || beatRecord
                )
            }
            viewModelScope.launch {
                repository.recordGameProgress(currentScore, state.level, 0)
            }
            spawnNextPiece()
        }
    }

    private fun spawnNextPiece() {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING) return
        refillBagIfNeeded()
        val nextItem = bag.removeFirst()
        refillBagIfNeeded()
        val previewThree = (0 until 3).map { bag.elementAt(it) }

        val spawned = spawnPiece(nextItem.first, nextItem.second)
        if (!isValidPosition(state.board, spawned)) {
            triggerGameOver()
            return
        }

        _uiState.update {
            it.copy(
                activePiece = spawned,
                ghostY = computeGhostY(it.board, spawned),
                canHold = true,
                nextQueue = previewThree
            )
        }
    }

    private fun triggerGameOver() {
        gravityJob?.cancel()
        lockDelayJob?.cancel()
        soundEngine.playGameOver()
        emitHaptic(HapticCue.GAME_OVER)
        val finalScore = _uiState.value.score
        val finalLevel = _uiState.value.level
        viewModelScope.launch {
            repository.recordGameProgress(finalScore, finalLevel, 0)
        }
        _uiState.update {
            it.copy(
                status = GameStatus.GAME_OVER,
                activePiece = null,
                ghostY = null
            )
        }
    }

    private fun startGravityLoop() {
        gravityJob?.cancel()
        gravityJob = viewModelScope.launch {
            while (true) {
                val state = _uiState.value
                if (state.status != GameStatus.PLAYING) break
                val intervalMs = dropIntervalMs(state.level)
                delay(intervalMs)
                val latest = _uiState.value
                if (latest.status == GameStatus.PLAYING &&
                    latest.clearingRows.isEmpty() &&
                    latest.activePiece != null
                ) {
                    tryMove(dx = 0, dy = 1, userTriggered = false)
                }
            }
        }
    }

    private fun dropIntervalMs(level: Int): Long {
        val steps = (level - 1).coerceAtLeast(0)
        val raw = 780L - (steps * 56L)
        return raw.coerceAtLeast(85L)
    }

    private fun spawnPiece(type: TetrominoType, hueOffset: Float): ActivePiece {
        return ActivePiece(
            type = type,
            rotation = 0,
            x = 3,
            y = 0,
            hueOffset = hueOffset
        )
    }

    private fun isValidPosition(board: List<List<LockedCell?>>, piece: ActivePiece): Boolean {
        for (cell in piece.worldCells()) {
            if (cell.col !in 0 until BOARD_COLS) return false
            if (cell.row >= BOARD_ROWS) return false
            if (cell.row >= 0 && board[cell.row][cell.col] != null) return false
        }
        return true
    }

    private fun isGrounded(board: List<List<LockedCell?>>, piece: ActivePiece): Boolean {
        return !isValidPosition(board, piece.copy(y = piece.y + 1))
    }

    private fun computeGhostY(board: List<List<LockedCell?>>, piece: ActivePiece): Int {
        var y = piece.y
        while (isValidPosition(board, piece.copy(y = y + 1))) {
            y++
        }
        return y
    }

    private fun addScore(points: Int) {
        if (points <= 0) return
        _uiState.update { state ->
            val updated = state.score + points
            state.copy(
                score = updated,
                highScore = max(state.highScore, updated),
                isNewRecord = state.isNewRecord || (updated > state.highScore && state.highScore > 0)
            )
        }
    }

    private fun emitHaptic(cue: HapticCue) {
        if (_uiState.value.statsAndSettings.hapticsEnabled) {
            _hapticCues.tryEmit(cue)
        }
    }

    fun updateColorShiftMode(mode: ColorShiftMode) {
        viewModelScope.launch { repository.setColorShiftMode(mode) }
    }

    fun updateColorShiftSpeed(speed: ColorShiftSpeed) {
        viewModelScope.launch { repository.setColorShiftSpeed(speed) }
    }

    fun toggleSound() {
        val next = !_uiState.value.statsAndSettings.soundEnabled
        viewModelScope.launch { repository.setSoundEnabled(next) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setSoundEnabled(enabled) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setHapticsEnabled(enabled) }
    }

    fun setGesturesEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setGesturesEnabled(enabled) }
    }

    fun setGhostEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setGhostEnabled(enabled) }
    }

    override fun onCleared() {
        super.onCleared()
        gravityJob?.cancel()
        lockDelayJob?.cancel()
        soundEngine.release()
    }
}
