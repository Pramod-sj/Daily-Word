package com.pramod.dailyword.framework.ui.notification_consent

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.pramod.dailyword.framework.ui.common.ThemedActivity
import com.pramod.dailyword.framework.ui.common.exts.openHomePage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationConsentActivity : ThemedActivity() {

    @Inject
    lateinit var importantPermissionState: ImportantPermissionState

    private val notificationPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            if (permissionGranted) {
                openHomePage(withFadeAnimation = true, finish = true)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotificationConsentScreen(
                skipCallback = {
                    openHomePage(withFadeAnimation = true, finish = true)
                },
                imInCallback = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionResult.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        openHomePage(withFadeAnimation = true, finish = true)
                    }
                },
                neverShowAgainCallback = {
                    importantPermissionState.markFullNotificationRequestDismissed()
                    openHomePage(withFadeAnimation = true, finish = true)
                }
            )
        }
    }

}