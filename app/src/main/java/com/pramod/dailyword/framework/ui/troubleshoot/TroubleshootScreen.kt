package com.pramod.dailyword.framework.ui.troubleshoot

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.themeadapter.appcompat.AppCompatTheme
import com.pramod.dailyword.R

@Preview
@Composable
fun PreviewTroubleshootScreen() {
    TroubleshootScreen()
}

@Composable
fun TroubleshootScreen(
    backButtonClick: () -> Unit = {},
    isNotificationEnabled: Boolean = false,
    isBatteryOptimizationDisabled: Boolean = false,
    isSetAlarmEnabled: Boolean = false,
    isUnusedAppPausingDisabled: Boolean = false,
    disableBatteryOptimizationClick: () -> Unit = {},
    enableNotificationClick: () -> Unit = {},
    allowSettingAlarmsClick: () -> Unit = {},
    unusedAppPausingDisableClick: () -> Unit = { },
) {

    val windowInsets = WindowInsets(
        top = WindowInsets.statusBars.getTop(LocalDensity.current),
    )

    val lottieRawComposition by rememberLottieComposition(spec = LottieCompositionSpec.Asset("all_okay.json"))

    AppCompatTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(id = R.string.troubleshoot_title))
                    },
                    windowInsets = windowInsets,
                    navigationIcon = {
                        IconButton(onClick = {
                            backButtonClick()
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_round_back_arrow),
                                contentDescription = "Back"
                            )
                        }
                    },
                    backgroundColor = MaterialTheme.colors.surface
                )
            },
        ) {

            if (!isNotificationEnabled
                || !isBatteryOptimizationDisabled
                || !isSetAlarmEnabled
                || !isUnusedAppPausingDisabled
            ) {
                BoxWithConstraints {

                    val maxWidth = remember(maxWidth) { maxWidth }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(it)
                            .padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        val modifier = remember(maxWidth) {
                            if (maxWidth > 600.dp)
                                Modifier.width(maxWidth * 0.5f)
                            else
                                Modifier.padding(horizontal = 16.dp)
                        }

                        if (!isNotificationEnabled) {
                            TroubleshootCard(
                                modifier = modifier,
                                title = stringResource(id = R.string.troubleshoot_card_notification_title),
                                subtitle = stringResource(id = R.string.troubleshoot_card_notification_desc),//"Please allow us to send daily word notification",
                                buttonLabel = stringResource(id = R.string.troubleshoot_card_notification_btn)//"Enable notification",
                            ) {
                                enableNotificationClick()
                            }
                        }

                        if (!isBatteryOptimizationDisabled) {
                            TroubleshootCard(
                                modifier = modifier,
                                title = stringResource(id = R.string.troubleshoot_card_battery_optimization_title),// "Disable Battery Optimization",
                                subtitle = stringResource(id = R.string.troubleshoot_card_battery_optimization_desc),//"Your devices is set to stop the app to save some battery, this setting prevent you learning new word!",
                                buttonLabel = stringResource(id = R.string.troubleshoot_card_battery_optimization_btn),//"Disable battery optimization",
                            ) {
                                disableBatteryOptimizationClick()
                            }
                        }

                        if (!isSetAlarmEnabled) {
                            TroubleshootCard(
                                modifier = modifier,
                                title = stringResource(id = R.string.troubleshoot_card_exact_alarms_title),//"Allow setting alarms",
                                subtitle = stringResource(id = R.string.troubleshoot_card_exact_alarms_desc),//"Please allow us set alarms for correct functioning of the app",
                                buttonLabel = stringResource(id = R.string.troubleshoot_card_exact_alarms_btn),//"Grant permission",
                            ) {
                                allowSettingAlarmsClick()
                            }
                        }

                        if (!isUnusedAppPausingDisabled) {
                            TroubleshootCard(
                                modifier = modifier,
                                title = stringResource(id = R.string.troubleshoot_card_unused_app_title),
                                subtitle = stringResource(id = R.string.troubleshoot_card_unused_app_desc),//"Please allow us to send daily word notification",
                                buttonLabel = stringResource(id = R.string.troubleshoot_card_unused_app_btn)//"Enable notification",
                            ) {
                                unusedAppPausingDisableClick()
                            }
                        }

                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        LottieAnimation(
                            modifier = Modifier.size(60.dp),
                            composition = lottieRawComposition,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(id = R.string.troubleshoot_all_good_message),//"All good!",
                            style = MaterialTheme.typography.h5
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun TroubleshootCard(
    modifier: Modifier,
    title: String,
    subtitle: String,
    buttonLabel: String,
    buttonCallback: () -> Unit,
) {
    val strokeColor = colorResource(id = R.color.update_card_stroke_color)
    Card(
        modifier = modifier,
        elevation = 0.dp,
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.body1.copy(
                    fontWeight = FontWeight(500),
                    color = colorResource(id = R.color.textColor_highEmphasis)
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.body2.copy(
                    color = colorResource(id = R.color.textColor_mediumEmphasis)
                )
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    buttonCallback()
                }) {
                Text(text = buttonLabel)
            }
        }
    }
}