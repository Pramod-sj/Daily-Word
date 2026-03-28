package com.pramod.games.crossword

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- THE RESPONSIVE KEYBOARD ---
@Composable
internal fun GridKeyboard(
    onKeyPress: (Char) -> Unit,
    onBackspacePress: () -> Unit,
    onToggleDirection: () -> Unit, // ✅ NEW: Callback for the toggle action
) {
    val keys =
        listOf(
            listOf('Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'),
            listOf('A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L'),
            listOf('Z', 'X', 'C', 'V', 'B', 'N', 'M'),
        )

    val keyboardLock = remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            keys.forEachIndexed { index, row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                ) {
                    // Row 2 Left Spacer
                    if (index == 1) Spacer(modifier = Modifier.weight(0.5f))

                    // ✅ NEW: Row 3 Left Toggle Button (Replaces the Spacer)
                    if (index == 2) {
                        KeyButton(
                            isActionKey = true,
                            icon = Icons.Rounded.Sync, // A nice switch/swap icon
                            modifier = Modifier.weight(1.5f),
                            keyboardLock = keyboardLock,
                            onClick = onToggleDirection,
                        )
                    }

                    // The Letters
                    row.forEach { letter ->
                        KeyButton(
                            text = letter.toString(),
                            modifier = Modifier.weight(1f),
                            keyboardLock = keyboardLock,
                            onClick = { onKeyPress(letter) },
                        )
                    }

                    // Row 3: The Backspace Key
                    if (index == 2) {
                        KeyButton(
                            isActionKey = true,
                            icon = Icons.AutoMirrored.Rounded.Backspace, // Passed as parameter now
                            modifier = Modifier.weight(1.5f),
                            keyboardLock = keyboardLock,
                            onClick = onBackspacePress,
                        )
                    }

                    // Row 2 Right Spacer
                    if (index == 1) Spacer(modifier = Modifier.weight(0.5f))
                }
            }
        }
    }
}

@Composable
private fun KeyButton(
    modifier: Modifier = Modifier,
    text: String = "",
    isActionKey: Boolean = false,
    icon: ImageVector? = null, // ✅ NEW: Allows custom icons
    keyboardLock: MutableState<Boolean>,
    onClick: () -> Unit,
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    val currentElevation by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 1.dp,
        animationSpec = tween(durationMillis = 50),
        label = "key_elevation",
    )

    // Instantly snap the colors based on the pressed state and key type
    val containerColor =
        when {
            isActionKey -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        }

    val contentColor =
        when {
            isActionKey -> MaterialTheme.colorScheme.onSecondaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

    Surface(
        modifier =
            modifier
                .height(54.dp)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down =
                            awaitFirstDown(
                                requireUnconsumed = true,
                                pass = PointerEventPass.Initial,
                            )

                        if (keyboardLock.value) {
                            return@awaitEachGesture
                        }

                        keyboardLock.value = true
                        val downTime = down.uptimeMillis

                        isPressed = true
                        view.performHapticFeedback(
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                                HapticFeedbackConstants.KEYBOARD_PRESS
                            } else {
                                HapticFeedbackConstants.KEYBOARD_TAP
                            },
                        )

                        val up = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                        keyboardLock.value = false

                        if (up != null) {
                            val duration = up.uptimeMillis - downTime
                            if (duration > 100L) {
                                view.performHapticFeedback(
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                                        HapticFeedbackConstants.KEYBOARD_RELEASE
                                    } else {
                                        HapticFeedbackConstants.KEYBOARD_TAP
                                    },
                                )
                            }
                            isPressed = false
                            onClick()
                        } else {
                            isPressed = false
                        }
                    }
                },
        shape = RoundedCornerShape(6.dp),
        color = containerColor,
        shadowElevation = currentElevation,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            // ✅ Check for the icon parameter here
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null, // decorative or handled by parent
                    tint = contentColor,
                    modifier = Modifier.size(24.dp),
                )
            } else {
                Text(
                    text = text,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = contentColor,
                )
            }
        }
    }
}
