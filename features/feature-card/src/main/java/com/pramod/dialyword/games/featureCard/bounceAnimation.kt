package com.pramod.dialyword.games.featureCard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

fun Modifier.bounceAnimation(
    key: String,
    enabled: Boolean = true,
    playCount: Int = 1,
    startDelayMs: Long = 0L
): Modifier {
    if (!enabled) return this
    return composed {
        var playsLeft by rememberSaveable(key) { mutableIntStateOf(playCount) }
        val scale = remember { Animatable(1f) }

        LaunchedEffect(playsLeft) {
            if (playsLeft <= 0) return@LaunchedEffect
            if (playsLeft == playCount) delay(startDelayMs)

            // subtle 3-step bounce — up, overshoot, settle
            scale.animateTo(
                targetValue = 1.04f,
                animationSpec = tween(durationMillis = 120, easing = EaseOut)
            )
            scale.animateTo(
                targetValue = 0.97f,
                animationSpec = tween(durationMillis = 100, easing = EaseInOut)
            )
            scale.animateTo(
                targetValue = 1.01f,
                animationSpec = tween(durationMillis = 80, easing = EaseInOut)
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 80, easing = EaseIn)
            )

            delay(400)
            playsLeft--
        }

        this.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
    }
}