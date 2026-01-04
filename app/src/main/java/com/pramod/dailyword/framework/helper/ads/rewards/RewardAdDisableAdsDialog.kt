package com.pramod.dailyword.framework.helper.ads.rewards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.themeadapter.appcompat.AppCompatTheme


sealed interface RewardButtonState {

    data object Loading : RewardButtonState

    data object AdReady : RewardButtonState

    data class AdFailedToLoad(val closingDialogInSec: Int) : RewardButtonState

}

@Preview
@Composable
fun RewardAdDisableAdsContent(
    modifier: Modifier = Modifier,
    buttonState: RewardButtonState = RewardButtonState.Loading,
    onWatchAdClick: () -> Unit = {},
    onDismissClick: () -> Unit = {}
) {

    val fbRemoteConfig = FBRemoteConfigCompositionLocal.current

    AppCompatTheme {

        Card(
            modifier = modifier
                .fillMaxWidth(),
            elevation = 8.dp,
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Title
                Text(
                    text = "Don’t want to see ads?",
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold
                )

                // Description
                Text(
                    text = "Watch a short reward ad and enjoy an ad-free experience for the next ${fbRemoteConfig?.getAdsConfig()?.disabledAdsDays ?: 7} days.",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                )

                // Info Row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Takes less than a minute",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {

                    Button(
                        onClick = onWatchAdClick,
                        shape = RoundedCornerShape(8.dp),
                        enabled = buttonState == RewardButtonState.AdReady
                    ) {
                        AnimatedVisibility(buttonState == RewardButtonState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        AnimatedVisibility(buttonState == RewardButtonState.AdReady) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Watch & Disable Ads"
                                )
                            }
                        }
                        AnimatedVisibility(buttonState is RewardButtonState.AdFailedToLoad) {
                            if (buttonState is RewardButtonState.AdFailedToLoad) {
                                Text(
                                    text = "Ad couldn’t load. Closing in ${buttonState.closingDialogInSec} sec"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = onDismissClick,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("No thanks")
                    }

                }
            }
        }
    }
}
