package com.pramod.dailyword.framework.ui.troubleshoot

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.pramod.dailyword.framework.ui.common.ThemedActivity
import com.pramod.dailyword.framework.ui.notification_consent.ImportantPermissionHandler
import com.pramod.dailyword.framework.ui.notification_consent.ImportantPermissionState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TroubleshootActivity : ThemedActivity() {

    @Inject
    lateinit var importantPermissionState: ImportantPermissionState

    @Inject
    lateinit var importantPermissionHandler: ImportantPermissionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val isNotificationEnabled by importantPermissionState.isNotificationEnabled.collectAsState(
                initial = false
            )

            val isBatteryOptimizationDisabled by importantPermissionState.isBatteryOptimizationDisabled.collectAsState(
                initial = false
            )

            val isSetAlarmEnabled by importantPermissionState.isSetAlarmEnabled.collectAsState(
                initial = false
            )

            val isUnusedAppPausingDisabled by importantPermissionState.isUnusedAppPausingDisabled
                .collectAsState(initial = false)

            TroubleshootScreen(
                backButtonClick = {
                    onBackPressedDispatcher.onBackPressed()
                },
                isNotificationEnabled = isNotificationEnabled,
                isBatteryOptimizationDisabled = isBatteryOptimizationDisabled,
                isSetAlarmEnabled = isSetAlarmEnabled,
                isUnusedAppPausingDisabled = isUnusedAppPausingDisabled,
                disableBatteryOptimizationClick = {
                    importantPermissionHandler.launchDisableBatteryOptimizationPermissionFlow()
                },
                allowSettingAlarmsClick = {
                    importantPermissionHandler.launchAllowSettingAlarmPermissionFlow()
                },
                enableNotificationClick = {
                    importantPermissionHandler.launchNotificationPermissionFlow()
                },
                unusedAppPausingDisableClick = {
                    importantPermissionHandler.launchDisableUnusedAppPaused()
                }
            )
        }
    }
}