package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class ArcadeSoundEngine {
    private val sampleRate = 22050
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    var isEnabled: Boolean = true

    private val moveSamples by lazy { synthesizeTone(freqStart = 320.0, freqEnd = 360.0, durationMs = 28, volume = 0.16) }
    private val rotateSamples by lazy { synthesizeTone(freqStart = 460.0, freqEnd = 620.0, durationMs = 45, volume = 0.22) }
    private val holdSamples by lazy { synthesizeTone(freqStart = 520.0, freqEnd = 390.0, durationMs = 55, volume = 0.22) }
    private val hardDropSamples by lazy { synthesizeTone(freqStart = 210.0, freqEnd = 95.0, durationMs = 85, volume = 0.34) }
    private val clearSingleSamples by lazy { synthesizeChord(listOf(523.25, 659.25), durationMs = 150, volume = 0.28) }
    private val clearMultiSamples by lazy { synthesizeChord(listOf(523.25, 659.25, 783.99, 1046.50), durationMs = 240, volume = 0.32) }
    private val levelUpSamples by lazy { synthesizeArpeggio(listOf(440.0, 554.37, 659.25, 880.0), stepMs = 55, volume = 0.28) }
    private val gameOverSamples by lazy { synthesizeArpeggio(listOf(392.0, 329.63, 261.63, 196.0), stepMs = 95, volume = 0.30) }

    fun playMove() = playAsync(moveSamples)
    fun playRotate() = playAsync(rotateSamples)
    fun playHold() = playAsync(holdSamples)
    fun playHardDrop() = playAsync(hardDropSamples)
    fun playLineClear(lines: Int) = playAsync(if (lines >= 2) clearMultiSamples else clearSingleSamples)
    fun playLevelUp() = playAsync(levelUpSamples)
    fun playGameOver() = playAsync(gameOverSamples)

    private fun playAsync(samples: ShortArray) {
        if (!isEnabled) return
        scope.launch {
            runCatching {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, samples.size)
                track.play()
                val durationMs = (samples.size * 1000L) / sampleRate + 60L
                kotlinx.coroutines.delay(durationMs)
                track.stop()
                track.release()
            }
        }
    }

    private fun synthesizeTone(freqStart: Double, freqEnd: Double, durationMs: Int, volume: Double): ShortArray {
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / count
            val freq = freqStart + (freqEnd - freqStart) * t
            phase += 2.0 * PI * freq / sampleRate
            val env = exp(-4.2 * t) * (1.0 - exp(-40.0 * t))
            val sample = (sin(phase) * env * volume * Short.MAX_VALUE).toInt()
            out[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    private fun synthesizeChord(freqs: List<Double>, durationMs: Int, volume: Double): ShortArray {
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / count
            val env = exp(-3.0 * t) * (1.0 - exp(-45.0 * t))
            var sum = 0.0
            for (f in freqs) {
                sum += sin(2.0 * PI * f * i / sampleRate)
            }
            val sample = ((sum / freqs.size) * env * volume * Short.MAX_VALUE).toInt()
            out[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    private fun synthesizeArpeggio(freqs: List<Double>, stepMs: Int, volume: Double): ShortArray {
        val stepCount = (sampleRate * stepMs) / 1000
        val out = ShortArray(stepCount * freqs.size)
        freqs.forEachIndexed { idx, freq ->
            val offset = idx * stepCount
            for (i in 0 until stepCount) {
                val t = i.toDouble() / stepCount
                val env = exp(-3.5 * t) * (1.0 - exp(-45.0 * t))
                val sample = (sin(2.0 * PI * freq * i / sampleRate) * env * volume * Short.MAX_VALUE).toInt()
                out[offset + i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }
        return out
    }

    fun release() {
        scope.cancel()
    }
}
