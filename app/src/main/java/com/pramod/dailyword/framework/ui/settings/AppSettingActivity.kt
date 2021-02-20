package com.pramod.dailyword.framework.ui.settings

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.map
import com.google.firebase.messaging.FirebaseMessaging
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityAppSettingBinding
import com.pramod.dailyword.framework.prefmanagers.NotificationPrefManager
import com.pramod.dailyword.framework.prefmanagers.ThemeManager
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.Message
import com.pramod.dailyword.framework.ui.common.exts.DailogHelper
import com.pramod.dailyword.framework.ui.common.exts.openAboutPage
import com.pramod.dailyword.framework.helper.*
import com.pramod.dailyword.framework.util.CommonUtils
import dagger.hilt.android.AndroidEntryPoint
import dev.doubledot.doki.ui.DokiActivity
import javax.inject.Inject

@AndroidEntryPoint
class AppSettingActivity : BaseActivity<ActivityAppSettingBinding, AppSettingViewModel>() {

    override val layoutId: Int = R.layout.activity_app_setting
    override val viewModel: AppSettingViewModel by viewModels()
    override val bindingVariable: Int = BR.appSettingViewModel

    @Inject
    lateinit var notificationPrefManager: NotificationPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpToolbar()
        handleUserCase()
        initThemeValue()
        initEdgeToEdgeValue()
        initWindowAnimValue()
        initNotificationValues()
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.title = null
        }
        binding.toolbar.setNavigationIcon(R.drawable.ic_round_back_arrow)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initThemeValue() {
        mViewModel.themeValue = themeManager.liveData().map {
            return@map CommonUtils.capitalizeFirstLetter(ThemeManager.getThemeNameFromOrdinal(it))
        }
    }

    private fun initEdgeToEdgeValue() {
        mViewModel.edgeToEdgeValue = windowPrefManager.getLiveData().map {
            return@map it
        }
    }

    private fun initWindowAnimValue() {
        mViewModel.windowAnimValue = windowAnimPrefManager.liveData().map {
            return@map it
        }
    }

    private fun initNotificationValues() {
        mViewModel.dailyWordNotificationValue =
            notificationPrefManager.getDailyWordNotificationEnabledLiveData().map {
                return@map it
            }
        mViewModel.reminderNotificationValue =
            notificationPrefManager.getReminderNotificationEnabledLiveData().map {
                return@map it
            }
    }


    private fun handleUserCase() {
        mViewModel.settingUseCase = object : SettingUseCase {
            override fun openChooseThemeDialog() {
                DailogHelper.showRadioDialog(
                    this@AppSettingActivity,
                    "Choose App Theme",
                    R.array.theme_options,
                    themeManager.getDefaultThemeModeOption().name,
                    "Apply",
                    "Cancel"
                ) { selectedThemeText ->
                    themeManager.applyTheme(ThemeManager.Options.valueOf(selectedThemeText))
                }
            }

            override fun toggleWindowAnimation() {
                windowAnimPrefManager.toggleWindowAnimationEnabled()
                Log.i(TAG, "toggleWindowAnimation: "+windowPrefManager.isEdgeToEdgeEnabled())
            }

            override fun toggleEdgeToEdge() {
                windowPrefManager.toggleEdgeToEdgeEnabled()
            }

            override fun toggleDailyWordNotification() {
                notificationPrefManager.toggleDailyWordNotification()
            }

            override fun toggleReminderNotification() {
                notificationPrefManager.toggleReminderNotification()
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
                        mViewModel.setMessage(Message.SnackBarMessage("Your token has been captured!"))
                    } else {
                        mViewModel.setMessage(Message.SnackBarMessage("Sorry we're currently not able to fetch your token"))
                    }
                }
            }

        }
    }

}
