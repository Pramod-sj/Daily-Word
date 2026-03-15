package com.pramod.dialyword.games.featureCard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

fun Modifier.glitterShimmer(
    key: String,
    playCount: Int = 1,
    startDelayMs: Long = 0L,
    enabled: Boolean = true
): Modifier {
    if (!enabled || playCount <= 0) return this
    return composed {
        var playsLeft by rememberSaveable(key) { mutableIntStateOf(playCount) }
        val sweepProgress = remember { Animatable(-0.15f) }

        val particles = remember {
            List(40) {
                Triple(
                    Random.nextFloat(),
                    Random.nextFloat(),
                    Random.nextFloat() * 1.8f + 0.6f
                )
            }
        }

        LaunchedEffect(playsLeft) {
            if (playsLeft <= 0) return@LaunchedEffect
            if (playsLeft == playCount) {
                delay(startDelayMs)
            }
            sweepProgress.snapTo(-0.15f)
            sweepProgress.animateTo(
                targetValue = 1.15f,
                animationSpec = tween(durationMillis = 1200, easing = LinearEasing)
            )
            delay(400)
            playsLeft--
        }

        if (playsLeft > 0) {
            val sweep = sweepProgress.value
            drawWithContent {
                // draw the actual composable content first
                drawContent()

                val w = size.width
                val h = size.height
                val glowWindow = 0.16f

                particles.forEach { (nx, ny, starSize) ->
                    val dist = abs(nx - sweep)
                    if (dist < glowWindow) {
                        val t = 1f - (dist / glowWindow)
                        val alpha = t * t * 0.55f
                        withTransform({
                            translate(nx * w, ny * h)
                        }) {
                            val r = starSize.dp.toPx()
                            val path = Path().apply {
                                moveTo(0f, -r * 2.2f)
                                lineTo(r * 0.3f, -r * 0.3f)
                                lineTo(r * 2.2f, 0f)
                                lineTo(r * 0.3f, r * 0.3f)
                                lineTo(0f, r * 2.2f)
                                lineTo(-r * 0.3f, r * 0.3f)
                                lineTo(-r * 2.2f, 0f)
                                lineTo(-r * 0.3f, -r * 0.3f)
                                close()
                            }
                            drawPath(path, color = Color.White.copy(alpha = alpha))
                        }
                    }
                }

                // faint cohesion band
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.055f),
                            Color.Transparent
                        ),
                        startX = (sweep - 0.12f) * w,
                        endX = (sweep + 0.12f) * w
                    ),
                    size = size
                )
            }
        } else {
            this // no-op once plays exhausted
        }
    }
}