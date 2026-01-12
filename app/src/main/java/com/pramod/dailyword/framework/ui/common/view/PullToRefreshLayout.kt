package com.pramod.dailyword.framework.ui.common.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewTreeObserver
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.pramod.dailyword.WOTDApp
import kotlin.math.min

/**
 * A custom [SwipeRefreshLayout] that provides haptic feedback during pull-to-refresh gestures.
 *
 * ## Features
 * - Progressive haptic ticks as the user pulls down
 * - Strong confirmation haptic when the refresh threshold is reached
 * - Completion haptic when refresh finishes
 * - Only triggers haptics for user-initiated refreshes (not programmatic `isRefreshing = true`)
 *
 * ## How It Works
 * The layout observes the internal CircleImageView (spinner) position using a [ViewTreeObserver.OnPreDrawListener].
 * As the spinner moves down during a pull gesture, haptic feedback is triggered at regular intervals.
 *
 * ## Usage
 * Use this as a drop-in replacement for [SwipeRefreshLayout]:
 *
 * <com.pramod.dailyword.framework.ui.common.view.PullToRefreshLayout
 *     android:id="@+id/swipeRefresh"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent">
 *
 *     <androidx.recyclerview.widget.RecyclerView ... />
 *
 * </com.pramod.dailyword.framework.ui.common.view.PullToRefreshLayout>
 */
class PullToRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SwipeRefreshLayout(context, attrs) {

    private var lastHapticProgress = 0f
    private var hasTriggeredThreshold = false
    private var spinnerView: View? = null
    private var isTracking = false

    private var spinnerStartY = -1f
    private var lastSpinnerY = -1f

    private var wasRefreshing = false

    // Track if refresh was initiated by user pull (not programmatic)
    private var wasUserInitiatedRefresh = false

    private val tickCount = 12

    private val triggerDistance: Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        64f,
        context.resources.displayMetrics
    )

    private val hapticFeedbackManager =
        (context.applicationContext as WOTDApp).hapticFeedbackManager

    private val preDrawListener = ViewTreeObserver.OnPreDrawListener {
        if (wasRefreshing && !isRefreshing) {
            // Only haptic on completion if user initiated the refresh
            if (wasUserInitiatedRefresh) {
                performRefreshCompleteHaptic()
            }
            wasRefreshing = false
            wasUserInitiatedRefresh = false
            resetState()
        } else if (isRefreshing) {
            // Mark as user-initiated only if we tracked the pull gesture
            if (hasTriggeredThreshold) {
                wasUserInitiatedRefresh = true
            }
            wasRefreshing = true
            resetState()
        } else {
            spinnerView?.let { spinner ->
                val spinnerY = spinner.y

                if (!isTracking) {
                    if (lastSpinnerY >= 0 && spinnerY > lastSpinnerY) {
                        spinnerStartY = lastSpinnerY
                        isTracking = true
                        lastHapticProgress = 0f
                        hasTriggeredThreshold = false
                    }
                    lastSpinnerY = spinnerY
                } else {
                    val pullDistance = spinnerY - spinnerStartY

                    if (pullDistance > 0) {
                        handleSpinnerProgress(pullDistance)
                    } else {
                        resetState()
                    }

                    lastSpinnerY = spinnerY
                }
            }
        }
        true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        findSpinnerView()
        viewTreeObserver.addOnPreDrawListener(preDrawListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewTreeObserver.removeOnPreDrawListener(preDrawListener)
    }

    private fun findSpinnerView() {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.javaClass.simpleName == "CircleImageView") {
                spinnerView = child
                lastSpinnerY = child.y
                break
            }
        }
    }

    private fun resetState() {
        isTracking = false
        lastHapticProgress = 0f
        hasTriggeredThreshold = false
        spinnerStartY = -1f
    }

    private fun handleSpinnerProgress(pullDistance: Float) {
        if (triggerDistance <= 0f) return

        val progress = min(pullDistance / triggerDistance, 1.2f)

        val currentTick = (min(progress, 1f) * tickCount).toInt()
        val lastTick = (lastHapticProgress * tickCount).toInt()

        if (currentTick > lastTick && progress < 1f) {
            performTickHaptic()
            lastHapticProgress = min(progress, 1f)
        }

        if (progress >= 1f && !hasTriggeredThreshold) {
            performThresholdHaptic()
            hasTriggeredThreshold = true
            lastHapticProgress = 1f
        }
    }

    private fun performTickHaptic() {
        if (hapticFeedbackManager.deviceVibrator == null) return
        val hapticType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            HapticFeedbackConstants.TEXT_HANDLE_MOVE
        } else {
            HapticFeedbackConstants.CLOCK_TICK
        }
        performHapticFeedback(hapticType)
    }

    private fun performThresholdHaptic() {
        if (hapticFeedbackManager.deviceVibrator == null) return
        val hapticType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            HapticFeedbackConstants.KEYBOARD_PRESS
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }
        performHapticFeedback(hapticType)
    }

    private fun performRefreshCompleteHaptic() {
        if (hapticFeedbackManager.deviceVibrator == null) return
        val hapticType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            HapticFeedbackConstants.KEYBOARD_PRESS
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }
        performHapticFeedback(hapticType)
    }
}