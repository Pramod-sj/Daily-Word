package com.pramod.dailyword.framework.ui.notification_consent

import android.content.res.Configuration
import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.themeadapter.appcompat.AppCompatTheme
import com.pramod.dailyword.R


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewNotificationConsentScreen() {
    NotificationConsentScreen()
}

@Composable
fun NotificationConsentScreen(
    imInCallback: () -> Unit = {},
    neverShowAgainCallback: () -> Unit = {},
    skipCallback: () -> Unit = {},
) {


    val lottieRawComposition by rememberLottieComposition(spec = LottieCompositionSpec.Asset("mobile_notification.json"))

    val windowInsets = WindowInsets(
        top = WindowInsets.statusBars.getTop(LocalDensity.current),
        bottom = WindowInsets.navigationBars.getBottom(LocalDensity.current)
    )

    AppCompatTheme {
        Scaffold(
            backgroundColor = colorResource(id = R.color.windowBackgroundColor),
            contentWindowInsets = windowInsets
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    TextButton(onClick = { skipCallback() }) {
                        Text(text = "Skip")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Image(
                    modifier = Modifier
                        .size(52.dp)
                        .padding(6.dp),
                    painter = painterResource(
                        id = R.drawable.ic_notify_bell
                    ),
                    contentDescription = "Bell"
                )
                Text(
                    text = "Get Notified",
                    style = MaterialTheme.typography.h6.copy(color = colorResource(id = R.color.textColor_highEmphasis))
                )
                Text(
                    text = "Never miss learning a new word every day!",
                    style = MaterialTheme.typography.body2.copy(
                        color = colorResource(id = R.color.textColor_highEmphasis)
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                LottieAnimation(
                    modifier = Modifier.size(300.dp),
                    composition = lottieRawComposition,
                    iterations = LottieConstants.IterateForever
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    TextButton(onClick = { neverShowAgainCallback() }) {
                        Text(text = "Don't show again")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(onClick = { imInCallback() }) {
                        Text(text = "I'm in")
                    }
                }
            }
        }
    }

}