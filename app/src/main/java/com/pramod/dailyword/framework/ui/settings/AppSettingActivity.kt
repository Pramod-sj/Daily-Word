package com.pramod.dailyword.framework.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.paging.ExperimentalPagingApi
import com.google.firebase.messaging.FirebaseMessaging
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.WOTDApp
import com.pramod.dailyword.databinding.ActivityAppSettingBinding
import com.pramod.dailyword.framework.helper.*
import com.pramod.dailyword.framework.prefmanagers.NotificationPrefManager
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import com.pramod.dailyword.framework.prefmanagers.WindowAnimPrefManager
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.Message
import com.pramod.dailyword.framework.ui.common.exts.*
import com.pramod.dailyword.framework.util.CommonUtils
import dagger.hilt.android.AndroidEntryPoint
import dev.doubledot.doki.ui.DokiActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
class AppSettingActivity :
    BaseActivity<ActivityAppSettingBinding, AppSettingViewModel>(R.layout.activity_app_setting) {

    override val viewModel: AppSettingViewModel by viewModels()

    override val bindingVariable: Int = BR.appSettingViewModel

    @Inject
    lateinit var windowAnimPrefManager: WindowAnimPrefManager

    @Inject
    lateinit var notificationPrefManager: NotificationPrefManager

    @Inject
    lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpToolbar(binding.toolbar, null, true)
        handleUserCase()
        initThemeValue()
        initEdgeToEdgeValue()
        initWindowAnimValue()
        initNotificationValues()
        initHideBadgeValue()
    }

    private fun initThemeValue() {
        themeManager.liveData().observe(this) {
            viewModel.themeValue.value = it
        }
    }

    private fun initEdgeToEdgeValue() {
        edgeToEdgePrefManager.getLiveData().observe(this) {
            viewModel.edgeToEdgeValue.value = it
        }
    }

    private fun initWindowAnimValue() {
        windowAnimPrefManager.liveData().observe(this) {
            viewModel.windowAnimValue.value = it
        }
    }


    private fun initHideBadgeValue() {
        prefManager.getHideBadgeLiveData().observe(this) {
            viewModel.hideBadgesValue.value = it
        }
    }

    private fun initNotificationValues() {

        notificationPrefManager.getDailyWordNotificationEnabledLiveData().observe(this) {
            viewModel.dailyWordNotificationValue.value = it
        }

        notificationPrefManager.getReminderNotificationEnabledLiveData().observe(this) {
            viewModel.reminderNotificationValue.value = it
        }
    }


    @ExperimentalCoroutinesApi
    @OptIn(ExperimentalPagingApi::class)
    private fun handleUserCase() {
        viewModel.settingUseCase = object : SettingUseCase {
            override fun openChooseThemeDialog() {
                DailogHelper.showRadioDialog(
                    this@AppSettingActivity,
                    "Choose App Theme",
                    R.array.theme_options,
                    themeManager.getThemeMode(),
                    "Apply",
                    "Cancel"
                ) { selectedThemeText ->
                    themeManager.applyTheme(selectedThemeText)
                }
            }

            override fun toggleWindowAnimation() {
                windowAnimPrefManager.toggle()
                Log.i(TAG, "toggleWindowAnimation: " + edgeToEdgePrefManager.isEnabled())
            }

            override fun toggleEdgeToEdge() {
                edgeToEdgePrefManager.toggle()
                restartActivity(true)
            }

            override fun toggleDailyWordNotification() {
                notificationPrefManager.toggleDailyWordNotification()
            }

            override fun toggleReminderNotification() {
                notificationPrefManager.toggleReminderNotification()

            }

            override fun toggleBadgeVisibility() {
                prefManager.toggleHideBadge()
            }

            override fun navigateToFacingNotificationIssue() {
                DokiActivity.start(
                    this@AppSettingActivity
                )
            }

            override fun navigateToAbout() {
                openAboutPage()
            }

            override fun copyFirebaseTokenId() {
                FirebaseMessaging.getInstance().token.addOnCompleteListener {
                    if (it.isSuccessful) {
                        CommonUtils.copyToClipboard(applicationContext, it.result)
                        viewModel.setMessage(Message.SnackBarMessage("Your token has been captured!"))
                    } else {
                        viewModel.setMessage(Message.SnackBarMessage("Sorry we're currently not able to fetch your token"))
                    }
                }
            }

            override fun clearAppData() {
                showBottomSheet(
                    "Clear App Data",
                    "Note: You'll loose all your bookmarks and word view data if you proceed",
                    positiveText = "Proceed",
                    positiveClickCallback = {
                        val cleared = WOTDApp.clearAppData(this@AppSettingActivity)
                        if (cleared) {
                            openSplashScreen(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                            )
                        }
                    },
                    negativeText = "Cancel",
                    negativeClickCallback = {

                    }
                )
            }

        }
    }

}
