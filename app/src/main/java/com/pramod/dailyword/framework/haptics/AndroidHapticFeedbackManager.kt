package com.pramod.dailyword.framework.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.pramod.dailyword.framework.prefmanagers.HapticPref
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidHapticFeedbackManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val hapticPref: HapticPref
) : HapticFeedbackManager {

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override val deviceVibrator: Vibrator?
        get() = if (!vibrator.hasVibrator() || !hapticPref.isHapticEnabled()) null else vibrator

    override fun perform(type: HapticType) {
        if (!vibrator.hasVibrator() || !hapticPref.isHapticEnabled()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (type) {
                HapticType.CLICK -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    } else {
                        VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                }

                HapticType.CONFIRM -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    } else {
                        VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                }

                HapticType.SUCCESS -> {
                    val timings = longArrayOf(0, 12, 30, 16)
                    val amplitudes = intArrayOf(0, 80, 0, 140)
                    VibrationEffect.createWaveform(timings, amplitudes, -1)
                }

                HapticType.ERROR -> {
                    val timings = longArrayOf(0, 20, 25, 20)
                    val amplitudes = intArrayOf(0, 180, 0, 200)
                    VibrationEffect.createWaveform(timings, amplitudes, -1)
                }

                HapticType.SWITCH_ON -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        VibrationEffect.startComposition()
                            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.40f)
                            .compose()
                    } else {
                        VibrationEffect.createOneShot(20, 36)
                    }
                }

                HapticType.SWITCH_OFF -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        VibrationEffect.startComposition()
                            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.33f)
                            .compose()
                    } else {
                        VibrationEffect.createOneShot(18, 34)
                    }
                }
            }
            vibrator.vibrate(effect)
        } else {
            // Legacy fallbacks for API < 26
            @Suppress("DEPRECATION")
            when (type) {
                HapticType.CLICK -> vibrator.vibrate(10)
                HapticType.CONFIRM -> vibrator.vibrate(20)
                HapticType.SUCCESS -> vibrator.vibrate(longArrayOf(0, 12, 30, 16), -1)
                HapticType.ERROR -> vibrator.vibrate(longArrayOf(0, 20, 25, 20), -1)
                HapticType.SWITCH_ON -> vibrator.vibrate(20)
                HapticType.SWITCH_OFF -> vibrator.vibrate(18)
            }
        }
    }
}
