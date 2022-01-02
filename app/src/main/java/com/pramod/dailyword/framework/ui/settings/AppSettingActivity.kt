package com.pramod.dailyword.framework.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.messaging.FirebaseMessaging
import com.pramod.dailyword.BR
import com.pramod.dailyword.Constants
import com.pramod.dailyword.R
import com.pramod.dailyword.WOTDApp
import com.pramod.dailyword.databinding.ActivityAppSettingBinding
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.helper.*
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import com.pramod.dailyword.framework.prefmanagers.WindowAnimPrefManager
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.Message
import com.pramod.dailyword.framework.ui.common.exts.*
import com.pramod.dailyword.framework.util.CommonUtils
import com.pramod.dailyword.framework.util.safeStartUpdateFlowForResult
import dagger.hilt.android.AndroidEntryPoint
import dev.doubledot.doki.ui.DokiActivity
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AppSettingActivity :
    BaseActivity<ActivityAppSettingBinding, AppSettingViewModel>(R.layout.activity_app_setting) {

    override val viewModel: AppSettingViewModel by viewModels()

    override val bindingVariable: Int = BR.appSettingViewModel

    private val appUpdateManager: AppUpdateManager by lazy {
        AppUpdateManagerFactory.create(this)
    }

    @Inject
    lateinit var windowAnimPrefManager: WindowAnimPrefManager

    @Inject
    lateinit var prefManager: PrefManager

    @Inject
    lateinit var fbRemoteConfig: FBRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpToolbar(binding.toolbar, null, true)
        handleUserCase()
        initThemeValue()
        initEdgeToEdgeValue()
        initWindowAnimValue()
        initHideBadgeValue()
        appUpdateManager.registerListener(installStateUpdatedListener)
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnCompleteListener { appUpdateInfoTask ->
            if (appUpdateInfoTask.isSuccessful) {
                when (appUpdateInfoTask.result.updateAvailability()) {
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                        if (appUpdateInfoTask.result.installStatus() == InstallStatus.DOWNLOADED) {
                            viewModel.subTitleCheckForUpdate.value =
                                AppSettingViewModel.DEFAULT_MESSAGE_NEW_UPDATE_AVAILABLE_TO_INSTALL
                        }
                    }
                    UpdateAvailability.UPDATE_AVAILABLE -> {
                        viewModel.subTitleCheckForUpdate.value =
                            AppSettingViewModel.DEFAULT_MESSAGE_NEW_UPDATE_AVAILABLE_TO_DOWNLOAD
                    }
                    UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                        viewModel.subTitleCheckForUpdate.value =
                            AppSettingViewModel.DEFAULT_MESSAGE_CHECK_FOR_UPDATE
                    }
                    UpdateAvailability.UNKNOWN -> {
                        viewModel.subTitleCheckForUpdate.value =
                            AppSettingViewModel.DEFAULT_MESSAGE_CHECK_FOR_UPDATE
                    }
                }
            } else {
                viewModel.subTitleCheckForUpdate.value =
                    AppSettingViewModel.DEFAULT_MESSAGE_CHECK_FOR_UPDATE
            }
        }
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
                Timber.i( "toggleWindowAnimation: " + edgeToEdgePrefManager.isEnabled())
            }

            override fun toggleEdgeToEdge() {
                edgeToEdgePrefManager.toggle()
                restartActivity(true)
            }

            override fun toggleBadgeVisibility() {
                prefManager.toggleHideBadge()
            }

            override fun navigateToFacingNotificationIssue() {
                DokiActivity.start(
                    this@AppSettingActivity
                )
            }

            override fun checkForUpdate() {

                if (viewModel.subTitleCheckForUpdate.value == AppSettingViewModel.DEFAULT_MESSAGE_NEW_UPDATE_DOWNLOADING) {
                    return
                }

                viewModel.subTitleCheckForUpdate.value = "Checking for the update..."

                appUpdateManager.appUpdateInfo.addOnCompleteListener { appUpdateInfoTask ->

                    if (appUpdateInfoTask.isSuccessful) {
                        when (appUpdateInfoTask.result.updateAvailability()) {
                            UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                                viewModel.subTitleCheckForUpdate.value = "Update in process..."

                                if (appUpdateInfoTask.result.installStatus() == InstallStatus.DOWNLOADED) {
                                    appUpdateManager.completeUpdate()
                                } else {
                                    appUpdateManager.safeStartUpdateFlowForResult(
                                        appUpdateInfoTask.result,
                                        AppUpdateType.FLEXIBLE,
                                        this@AppSettingActivity,
                                        Constants.APP_UPDATE_FLEX_REQUEST_CODE
                                    ) { e ->
                                        viewModel.setMessage(
                                            Message.ToastMessage(
                                                "Something went wrong during update process: reason:${e.message}"
                                            )
                                        )

                                    }
                                }
                            }
                            UpdateAvailability.UPDATE_AVAILABLE -> {
                                viewModel.subTitleCheckForUpdate.value =
                                    AppSettingViewModel.DEFAULT_MESSAGE_NEW_UPDATE_AVAILABLE_TO_DOWNLOAD
                                val releaseNote = fbRemoteConfig.getLatestRelease()
                                releaseNote?.let {
                                    showBottomSheet(
                                        title = "A new update version ${releaseNote.versionName} available!",
                                        desc = CommonUtils.formatListAsBulletList(releaseNote.changes),
                                        cancellable = true,
                                        positiveText = "Update",
                                        positiveClickCallback = {
                                            appUpdateManager.safeStartUpdateFlowForResult(
                                                appUpdateInfoTask.result,
                                                AppUpdateType.FLEXIBLE,
                                                this@AppSettingActivity,
                                                Constants.APP_UPDATE_FLEX_REQUEST_CODE
                                            ) { e ->
                                                viewModel.setMessage(
                                                    Message.ToastMessage(
                                                        "Something went wrong during update process: reason:${e.message}"
                                                    )
                                                )

                                            }

                                        },
                                        negativeText = "May be later",
                                    )
                                }
                            }
                            UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                                viewModel.subTitleCheckForUpdate.value =
                                    AppSettingViewModel.DEFAULT_MESSAGE_CHECK_FOR_UPDATE
                            }
                            UpdateAvailability.UNKNOWN -> {
                                viewModel.subTitleCheckForUpdate.value =
                                    AppSettingViewModel.DEFAULT_MESSAGE_CHECK_FOR_UPDATE
                            }
                        }
                    } else {
                        viewModel.subTitleCheckForUpdate.value =
                            AppSettingViewModel.DEFAULT_MESSAGE_CHECK_FOR_UPDATE
                        viewModel.setMessage(Message.ToastMessage("Failed to check update:" + appUpdateInfoTask.exception?.message))
                    }
                }
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

    private val installStateUpdatedListener = InstallStateUpdatedListener { installState ->
        when (installState.installStatus()) {
            InstallStatus.DOWNLOADED -> {
                viewModel.subTitleCheckForUpdate.value =
                    AppSettingViewModel.DEFAULT_MESSAGE_NEW_UPDATE_AVAILABLE_TO_INSTALL
            }
            InstallStatus.CANCELED -> {
                viewModel.subTitleCheckForUpdate.value =
                    AppSettingViewModel.DEFAULT_MESSAGE_NEW_UPDATE_AVAILABLE_TO_DOWNLOAD
                viewModel.setMessage(Message.ToastMessage("User cancelled update app process"))
            }
            InstallStatus.DOWNLOADING -> {
                viewModel.subTitleCheckForUpdate.value =
                    AppSettingViewModel.DEFAULT_MESSAGE_NEW_UPDATE_DOWNLOADING +
                            " " +
                            ((installState.bytesDownloaded() * 100) / installState.totalBytesToDownload()) + "%"
            }
            InstallStatus.FAILED -> {
                viewModel.setMessage(Message.ToastMessage("Update process failed! reason:${installState.installErrorCode()}"))
            }
            InstallStatus.INSTALLED -> {
                viewModel.setMessage(Message.ToastMessage("Successfully updated!"))
            }
            InstallStatus.INSTALLING -> {
                Toast.makeText(
                    applicationContext,
                    "Installation started!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            InstallStatus.PENDING -> {}
            InstallStatus.REQUIRES_UI_INTENT -> {
                viewModel.subTitleCheckForUpdate.value =
                    AppSettingViewModel.DEFAULT_MESSAGE_NEW_UPDATE_AVAILABLE_TO_DOWNLOAD
                //no need to implement
                Toast.makeText(
                    applicationContext,
                    "UI Intent issue!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            InstallStatus.UNKNOWN -> {
                viewModel.subTitleCheckForUpdate.value =
                    AppSettingViewModel.DEFAULT_MESSAGE_NEW_UPDATE_AVAILABLE_TO_DOWNLOAD
                Toast.makeText(
                    applicationContext,
                    "Unknown issue!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

}
