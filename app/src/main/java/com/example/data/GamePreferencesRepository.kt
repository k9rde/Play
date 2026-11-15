package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.game.ColorShiftMode
import com.example.game.ColorShiftSpeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "spectrum_blocks_prefs")

data class StoredGameStatsAndSettings(
    val highScore: Int = 0,
    val bestLevel: Int = 1,
    val totalLinesCleared: Int = 0,
    val colorShiftMode: ColorShiftMode = ColorShiftMode.CONTINUOUS_SPECTRUM,
    val colorShiftSpeed: ColorShiftSpeed = ColorShiftSpeed.MEDIUM,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val gesturesEnabled: Boolean = true,
    val ghostEnabled: Boolean = true
)

class GamePreferencesRepository(private val context: Context) {

    private object Keys {
        val HIGH_SCORE = intPreferencesKey("high_score")
        val BEST_LEVEL = intPreferencesKey("best_level")
        val TOTAL_LINES = intPreferencesKey("total_lines")
        val COLOR_MODE = stringPreferencesKey("color_mode")
        val COLOR_SPEED = stringPreferencesKey("color_speed")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val GESTURES_ENABLED = booleanPreferencesKey("gestures_enabled")
        val GHOST_ENABLED = booleanPreferencesKey("ghost_enabled")
    }

    val stateFlow: Flow<StoredGameStatsAndSettings> = context.dataStore.data.map { prefs ->
        val mode = prefs[Keys.COLOR_MODE]?.let { raw ->
            runCatching { ColorShiftMode.valueOf(raw) }.getOrNull()
        } ?: ColorShiftMode.CONTINUOUS_SPECTRUM

        val speed = prefs[Keys.COLOR_SPEED]?.let { raw ->
            runCatching { ColorShiftSpeed.valueOf(raw) }.getOrNull()
        } ?: ColorShiftSpeed.MEDIUM

        StoredGameStatsAndSettings(
            highScore = prefs[Keys.HIGH_SCORE] ?: 0,
            bestLevel = prefs[Keys.BEST_LEVEL] ?: 1,
            totalLinesCleared = prefs[Keys.TOTAL_LINES] ?: 0,
            colorShiftMode = mode,
            colorShiftSpeed = speed,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            gesturesEnabled = prefs[Keys.GESTURES_ENABLED] ?: true,
            ghostEnabled = prefs[Keys.GHOST_ENABLED] ?: true
        )
    }

    suspend fun recordGameProgress(score: Int, level: Int, newlyClearedLines: Int) {
        context.dataStore.edit { prefs ->
            val currentHigh = prefs[Keys.HIGH_SCORE] ?: 0
            if (score > currentHigh) {
                prefs[Keys.HIGH_SCORE] = score
            }
            val currentBestLevel = prefs[Keys.BEST_LEVEL] ?: 1
            if (level > currentBestLevel) {
                prefs[Keys.BEST_LEVEL] = level
            }
            if (newlyClearedLines > 0) {
                prefs[Keys.TOTAL_LINES] = (prefs[Keys.TOTAL_LINES] ?: 0) + newlyClearedLines
            }
        }
    }

    suspend fun setColorShiftMode(mode: ColorShiftMode) {
        context.dataStore.edit { it[Keys.COLOR_MODE] = mode.name }
    }

    suspend fun setColorShiftSpeed(speed: ColorShiftSpeed) {
        context.dataStore.edit { it[Keys.COLOR_SPEED] = speed.name }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setGesturesEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.GESTURES_ENABLED] = enabled }
    }

    suspend fun setGhostEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.GHOST_ENABLED] = enabled }
    }
}
