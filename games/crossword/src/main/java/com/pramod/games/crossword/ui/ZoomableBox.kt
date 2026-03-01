package com.pramod.games.crossword.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.launch
import kotlin.math.abs

data class WordFocusTarget(
    val startOffset: Offset,
    val centerOffset: Offset,
    val widthPx: Float,
    val heightPx: Float,
)

@Composable
fun ZoomableBox(
    modifier: Modifier = Modifier,
    minZoom: Float = 0.8f, // Allows zooming out slightly before snapping back
    maxZoom: Float = 4f,
    focusTarget: WordFocusTarget? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    // ✅ 1. Changed scale to an Animatable
    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val coroutineScope = rememberCoroutineScope()
    val decay = rememberSplineBasedDecay<Float>()

    LaunchedEffect(focusTarget, containerSize) {
        if (focusTarget != null && containerSize.width > 0) {
            // 1. Calculate how big the word currently appears on screen
            val scaledWordWidth = focusTarget.widthPx * scale.value
            val scaledWordHeight = focusTarget.heightPx * scale.value

            // 2. Does it fit? (20% margin)
            val fitsHorizontally = scaledWordWidth < (containerSize.width * 0.8f)
            val fitsVertically = scaledWordHeight < (containerSize.height * 0.8f)

            // 3. SMART DECISION: Evaluate X and Y independently!
            // If an Across word is too wide, only snap X to the start letter. Keep Y centered.
            // If a Down word is too tall, only snap Y to the top letter. Keep X centered.
            val targetX =
                if (fitsHorizontally) focusTarget.centerOffset.x else focusTarget.startOffset.x
            val targetY =
                if (fitsVertically) focusTarget.centerOffset.y else focusTarget.startOffset.y

            // Find the center of the screen viewport
            val gridCenterX = containerSize.width / 2f
            val gridCenterY = containerSize.height / 2f

            // Calculate the required movement for BOTH axes
            val desiredOffsetX = (gridCenterX - targetX) * scale.value
            val desiredOffsetY = (gridCenterY - targetY) * scale.value

            val maxX = ((containerSize.width * (scale.value - 1)) / 2f).coerceAtLeast(0f)
            val maxY = ((containerSize.height * (scale.value - 1)) / 2f).coerceAtLeast(0f)

            // Animate the camera for both axes simultaneously
            launch {
                offsetX.animateTo(desiredOffsetX.coerceIn(-maxX, maxX))
            }
            launch {
                offsetY.animateTo(desiredOffsetY.coerceIn(-maxY, maxY))
            }
        }
    }

    Box(
        modifier =
            modifier
                .onSizeChanged { containerSize = it }
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTransformGestures(
                        panZoomLock = true,
                        onGesture = { _, pan, zoom, _ ->
                            coroutineScope.launch {
                                // Update scale with bounds
                                val newScale = (scale.value * zoom).coerceIn(minZoom, maxZoom)
                                scale.snapTo(newScale)

                                val maxX =
                                    ((containerSize.width * (newScale - 1)) / 2f).coerceAtLeast(0f)
                                val maxY =
                                    ((containerSize.height * (newScale - 1)) / 2f).coerceAtLeast(0f)

                                offsetX.stop()
                                offsetY.stop()

                                offsetX.updateBounds(-maxX, maxX)
                                offsetY.updateBounds(-maxY, maxY)
                                offsetX.snapTo((offsetX.value + pan.x).coerceIn(-maxX, maxX))
                                offsetY.snapTo((offsetY.value + pan.y).coerceIn(-maxY, maxY))
                            }
                        },
                    )
                }.pointerInput(Unit) {
                    awaitEachGesture {
                        val velocityTracker = VelocityTracker()
                        var pointerCount = 0

                        awaitFirstDown()
                        do {
                            val event = awaitPointerEvent()
                            pointerCount = maxOf(pointerCount, event.changes.count { it.pressed })
                            event.changes.forEach { change ->
                                velocityTracker.addPosition(
                                    change.uptimeMillis,
                                    change.position,
                                )
                            }
                        } while (event.changes.any { it.pressed })

                        // ✅ 2. Gesture ended! If zoomed out, snap back to 1x and center
                        if (scale.value < 1f) {
                            coroutineScope.launch {
                                launch { scale.animateTo(1f) }
                                launch { offsetX.animateTo(0f) }
                                launch { offsetY.animateTo(0f) }
                            }
                            return@awaitEachGesture // Exit early, no fling needed
                        }

                        // Only fling if it was a single finger gesture (no pinch involved)
                        if (pointerCount > 1) return@awaitEachGesture

                        val velocity = velocityTracker.calculateVelocity()

                        // Only fling if velocity exceeds threshold
                        val minFlingVelocity = 200f
                        if (abs(velocity.x) < minFlingVelocity && abs(velocity.y) < minFlingVelocity) return@awaitEachGesture

                        coroutineScope.launch {
                            val maxX =
                                ((containerSize.width * (scale.value - 1)) / 2f).coerceAtLeast(0f)
                            val maxY =
                                ((containerSize.height * (scale.value - 1)) / 2f).coerceAtLeast(0f)

                            offsetX.updateBounds(-maxX, maxX)
                            offsetY.updateBounds(-maxY, maxY)

                            launch {
                                offsetX.animateDecay(
                                    initialVelocity = velocity.x,
                                    animationSpec = decay,
                                )
                            }
                            launch {
                                offsetY.animateDecay(
                                    initialVelocity = velocity.y,
                                    animationSpec = decay,
                                )
                            }
                        }
                    }
                }
                // ✅ 3. Used the lambda version of graphicsLayer for better performance
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    translationX = offsetX.value
                    translationY = offsetY.value
                },
        content = content,
    )
}
