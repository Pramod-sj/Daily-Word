package com.pramod.games.crossword.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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

internal data class WordFocusTarget(
    val startOffset: Offset,
    val centerOffset: Offset,
    val widthPx: Float,
    val heightPx: Float,
)

@Composable
internal fun ZoomableBox(
    modifier: Modifier = Modifier,
    minZoom: Float = 0.8f,
    maxZoom: Float = 4f,
    focusTarget: WordFocusTarget? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val coroutineScope = rememberCoroutineScope()
    val decay = rememberSplineBasedDecay<Float>()

    // Low stiffness makes the camera smoothly catch up to your typing rather than violently snapping.
    val cameraPanSpec = remember {
        spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        )
    }

    // We extract ONLY the exact geometry. If the focusTarget updates a string but
    // stays in the same coordinate, the animation won't needlessly restart.
    val targetStartX = focusTarget?.startOffset?.x
    val targetStartY = focusTarget?.startOffset?.y
    val targetCenterX = focusTarget?.centerOffset?.x
    val targetCenterY = focusTarget?.centerOffset?.y
    val targetWidth = focusTarget?.widthPx
    val targetHeight = focusTarget?.heightPx

    LaunchedEffect(
        targetStartX, targetStartY, targetCenterX, targetCenterY, targetWidth, targetHeight, containerSize
    ) {
        if (focusTarget != null && containerSize.width > 0) {

            // Use targetValue to calculate bounds so it doesn't glitch if a zoom is currently finishing
            val currentScale = scale.targetValue

            val scaledWordWidth = focusTarget.widthPx * currentScale
            val scaledWordHeight = focusTarget.heightPx * currentScale

            val fitsHorizontally = scaledWordWidth < (containerSize.width * 0.8f)
            val fitsVertically = scaledWordHeight < (containerSize.height * 0.8f)

            val targetX = if (fitsHorizontally) focusTarget.centerOffset.x else focusTarget.startOffset.x
            val targetY = if (fitsVertically) focusTarget.centerOffset.y else focusTarget.startOffset.y

            val gridCenterX = containerSize.width / 2f
            val gridCenterY = containerSize.height / 2f

            val desiredOffsetX = (gridCenterX - targetX) * currentScale
            val desiredOffsetY = (gridCenterY - targetY) * currentScale

            val maxX = ((containerSize.width * (currentScale - 1)) / 2f).coerceAtLeast(0f)
            val maxY = ((containerSize.height * (currentScale - 1)) / 2f).coerceAtLeast(0f)

            // Update bounds BEFORE animating to prevent stuttering against the invisible walls
            offsetX.updateBounds(-maxX, maxX)
            offsetY.updateBounds(-maxY, maxY)

            launch {
                offsetX.animateTo(
                    targetValue = desiredOffsetX.coerceIn(-maxX, maxX),
                    animationSpec = cameraPanSpec
                )
            }
            launch {
                offsetY.animateTo(
                    targetValue = desiredOffsetY.coerceIn(-maxY, maxY),
                    animationSpec = cameraPanSpec
                )
            }
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { containerSize = it }
            .clipToBounds()
            .pointerInput(Unit) {
                detectTransformGestures(
                    panZoomLock = true,
                    onGesture = { _, pan, zoom, _ ->
                        coroutineScope.launch {
                            // 🔥 FIX 4: Stop auto-panning immediately if the user touches the screen
                            offsetX.stop()
                            offsetY.stop()

                            val newScale = (scale.value * zoom).coerceIn(minZoom, maxZoom)
                            scale.snapTo(newScale)

                            val maxX = ((containerSize.width * (newScale - 1)) / 2f).coerceAtLeast(0f)
                            val maxY = ((containerSize.height * (newScale - 1)) / 2f).coerceAtLeast(0f)

                            offsetX.updateBounds(-maxX, maxX)
                            offsetY.updateBounds(-maxY, maxY)

                            offsetX.snapTo((offsetX.value + pan.x).coerceIn(-maxX, maxX))
                            offsetY.snapTo((offsetY.value + pan.y).coerceIn(-maxY, maxY))
                        }
                    },
                )
            }
            .pointerInput(Unit) {
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

                    if (scale.value < 1f) {
                        coroutineScope.launch {
                            launch { scale.animateTo(1f) }
                            launch { offsetX.animateTo(0f) }
                            launch { offsetY.animateTo(0f) }
                        }
                        return@awaitEachGesture
                    }

                    if (pointerCount > 1) return@awaitEachGesture

                    val velocity = velocityTracker.calculateVelocity()
                    val minFlingVelocity = 200f
                    if (abs(velocity.x) < minFlingVelocity && abs(velocity.y) < minFlingVelocity) return@awaitEachGesture

                    coroutineScope.launch {
                        val maxX = ((containerSize.width * (scale.value - 1)) / 2f).coerceAtLeast(0f)
                        val maxY = ((containerSize.height * (scale.value - 1)) / 2f).coerceAtLeast(0f)

                        offsetX.updateBounds(-maxX, maxX)
                        offsetY.updateBounds(-maxY, maxY)

                        launch { offsetX.animateDecay(initialVelocity = velocity.x, animationSpec = decay) }
                        launch { offsetY.animateDecay(initialVelocity = velocity.y, animationSpec = decay) }
                    }
                }
            }
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                translationX = offsetX.value
                translationY = offsetY.value
            },
        content = content,
    )
}
