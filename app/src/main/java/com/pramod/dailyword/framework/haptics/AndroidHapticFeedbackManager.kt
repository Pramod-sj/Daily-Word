package com.pramod.dailyword.framework.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidHapticFeedbackManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) : HapticFeedbackManager {

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun perform(type: HapticType) {
        if (!vibrator.hasVibrator()) return

        val effect = when (type) {

            HapticType.CLICK ->
                // Pixel-style light UI tick
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)

            HapticType.CONFIRM ->
                // Pixel confirmation click
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)

            HapticType.SUCCESS -> {
                // Subtle affirmation (transactional, not UI)
                val timings = longArrayOf(
                    0,
                    12,
                    30,
                    16
                )
                val amplitudes = intArrayOf(
                    0,
                    80,
                    0,
                    140
                )
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }

            HapticType.ERROR -> {
                // Clear but controlled error signal
                val timings = longArrayOf(
                    0,
                    20,
                    25,
                    20
                )
                val amplitudes = intArrayOf(
                    0,
                    180,
                    0,
                    200
                )
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            }

            HapticType.SWITCH_ON -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    VibrationEffect.startComposition()
                        .addPrimitive(
                            VibrationEffect.Composition.PRIMITIVE_TICK,
                            0.40f // Pixel-accurate ON
                        )
                        .compose()
                } else {
                    VibrationEffect.createOneShot(
                        20,
                        36 // soft fallback
                    )
                }
            }

            HapticType.SWITCH_OFF -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    VibrationEffect.startComposition()
                        .addPrimitive(
                            VibrationEffect.Composition.PRIMITIVE_TICK,
                            0.33f // Pixel-accurate OFF (barely lighter)
                        )
                        .compose()
                } else {
                    VibrationEffect.createOneShot(
                        18,
                        34
                    )
                }
            }
        }

        vibrator.vibrate(effect)
    }
}
