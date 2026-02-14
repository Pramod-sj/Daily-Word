package com.pramod.dailyword.framework.ui.notification_consent

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.pramod.dailyword.WOTDApp

@Composable
fun NotificationConsentLottieAnimation() {
    val context = LocalContext.current

    val composition by rememberLottieComposition(spec = LottieCompositionSpec.Asset("mobile_notification.json"))

    val progressState = animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    var hasVibratedByLoop by remember { mutableStateOf(false) }

    LaunchedEffect(progressState.progress) {
        val compositionDuration = composition?.durationFrames ?: return@LaunchedEffect
        val currentFrame = progressState.progress * compositionDuration

        //we calculated that mobile vibrate at 8th frame hence start the vibration effect
        if (currentFrame >= 8f && !hasVibratedByLoop) {
            triggerVibration(context)
            hasVibratedByLoop = true
        }

        if (currentFrame < 5f) {
            hasVibratedByLoop = false
        }
    }

    LottieAnimation(
        composition = composition,
        progress = { progressState.progress },
        modifier = Modifier.size(300.dp)
    )
}

private fun triggerVibration(context: Context) {
    val hapticFeedbackManager = (context.applicationContext as WOTDApp).hapticFeedbackManager
    hapticFeedbackManager.deviceVibrator?.let { vibrator ->
        // CHECK: Does the device support amplitude control? (Most modern phones do)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator.hasAmplitudeControl()) {
            // PREMIUM PATTERN: Varies intensity to feel "organic"
            // 1. Timings: How long each step lasts (ms)
            val timings = longArrayOf(
                0,  // Start immediately
                12, // Tick 1 (Short)
                50, // Gap
                12, // Tick 2
                50, // Gap
                12, // Tick 3
                50, // Gap
                12, // Tick 4
                50, // Gap
                12  // Tick 5
            )
            // 2. Amplitudes: Strength (0-255).
            // We stay low (40-70) for that "Subtle/Premium" feel.
            val amplitudes = intArrayOf(
                0,  // Delay has 0 amplitude
                50, // Soft tick
                0,  // Gap
                70, // Slightly stronger tick (Peak of the shake)
                0,  // Gap
                60, // Soft tick
                0,  // Gap
                45, // Fading out
                0,  // Gap
                30  // Very subtle finish
            )
            // -1 means "do not repeat"
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            // FALLBACK: For older phones or those without amplitude control
            // We make the pulses extremely short (10ms) to emulate crispness.
            val fallbackPattern = longArrayOf(0, 10, 50, 10, 50, 10, 50, 10, 50)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(fallbackPattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(fallbackPattern, -1)
            }
        }
    }
}