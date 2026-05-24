package com.pramod.dialyword.games.featureCard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

@Preview
@Composable
private fun Preview() {
    CrosswordTheme(false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glitterShimmer("test", 10)
                .height(60.dp)
        ) {

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )

        }
    }
}

fun Modifier.glitterShimmer(
    key: String,
    playCount: Int = 1,
    startDelayMs: Long = 0L,
    enabled: Boolean = true,
    glitterColor: Color = Color.Unspecified   // ← new optional param
): Modifier {
    if (!enabled || playCount <= 0) return this
    return composed {

        val resolvedColor = when {
            glitterColor != Color.Unspecified -> glitterColor
            MaterialTheme.colorScheme.surface.luminance() > 0.5f ->
                Color(0xFFFFB300)   // amber-gold; punches through light backgrounds
            else ->
                Color.White
        }

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
            if (playsLeft == playCount) delay(startDelayMs)
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
                drawContent()

                val w = size.width
                val h = size.height
                val glowWindow = 0.16f

                particles.forEach { (nx, ny, starSize) ->
                    val dist = abs(nx - sweep)
                    if (dist < glowWindow) {
                        val t = 1f - (dist / glowWindow)
                        val alpha = t * t * 0.55f         // same formula, color drives contrast
                        withTransform({ translate(nx * w, ny * h) }) {
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
                            drawPath(path, color = resolvedColor.copy(alpha = alpha))
                        }
                    }
                }

                // faint cohesion band — same resolved color
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            resolvedColor.copy(alpha = 0.055f),
                            Color.Transparent
                        ),
                        startX = (sweep - 0.12f) * w,
                        endX = (sweep + 0.12f) * w
                    ),
                    size = size
                )
            }
        } else {
            this
        }
    }
}