package com.pramod.games.crossword

import android.view.WindowManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
internal fun GlobalConfettiOverlay() {
    val play by GlobalOverlayController.playConfetti.collectAsState()

    if (play) {
        Dialog(
            onDismissRequest = { /* Controlled by Lottie finishing */ },
            properties =
                DialogProperties(
                    usePlatformDefaultWidth = false, // Fill the screen
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                ),
        ) {
            val view = LocalView.current
            DisposableEffect(Unit) {
                val window = (view.parent as? DialogWindowProvider)?.window
                if (window != null) {
                    // 1. Stop this window from stealing the dim from the dialog beneath it
                    window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

                    // 2. Make it a "Ghost" window so touches pass right through
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    )
                }
                onDispose { }
            }

            FullScreenConfetti(
                modifier = Modifier.fillMaxSize(),
                onFinished = { GlobalOverlayController.onConfettiFinished() },
            )
        }
    }
}

// This can be accessed from ANY ViewModel or screen in your app
object GlobalOverlayController {
    private val _playConfetti = MutableStateFlow(false)
    val playConfetti = _playConfetti.asStateFlow()

    fun triggerConfetti() {
        _playConfetti.value = true
    }

    fun onConfettiFinished() {
        _playConfetti.value = false
    }
}

@Composable
fun FullScreenConfetti(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit,
) {
    // 1. Load the animation file from res/raw/confetti.json
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.confetti))

    // 2. Drive the animation (Play exactly once)
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1, // Stop after one loop
        isPlaying = true, // Auto-start the moment it hits the screen
    )

    // 3. Listen for the exact frame the animation ends
    LaunchedEffect(progress) {
        if (progress == 1f) {
            onFinished()
        }
    }

    // 4. Render the Lottie
    // We only render it if it hasn't finished, keeping the UI tree clean
    if (progress < 1f) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = modifier.fillMaxSize(),
            // 🔥 Ensures the confetti reaches the very edges of tall phones!
            contentScale = ContentScale.Crop,
        )
    }
}
