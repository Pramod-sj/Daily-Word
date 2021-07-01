package com.pramod.dailyword.framework.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.paging.ExperimentalPagingApi
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpToolbar(binding.toolbar, null, true)
        handleUserCase()
        initThemeValue()
        initEdgeToEdgeValue()
        initWindowAnimValue()
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

                appUpdateManager.registerListener(installStateUpdatedListener)

                appUpdateManager.appUpdateInfo.addOnCompleteListener { appUpdateInfoTask ->
                    if (appUpdateInfoTask.isSuccessful) {

                        val result = appUpdateInfoTask.result

                        if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {

                            val releaseNote = fbRemoteConfig.getLatestReleaseNote()

                            if (releaseNote == null) {
                                //release note will be empty when update is available
                                //but latest release note is not updated on firebase remote config
                                appUpdateManager.safeStartUpdateFlowForResult(
                                    result,
                                    AppUpdateType.FLEXIBLE,
                                    this@AppSettingActivity,
                                    Constants.APP_UPDATE_FLEX_REQUEST_CODE
                                ) { e ->
                                    viewModel.setMessage(
                                        Message.ToastMessage(
                                            "Something went wrong while updating: reason:${e.message}"
                                        )
                                    )

                                }
                                return@addOnCompleteListener
                            }

                            viewModel.subTitleCheckForUpdate.value =
                                if (result.installStatus() == InstallStatus.DOWNLOADED) {
                                    AppSettingViewModel.DEFAULT_MESSAGE_NEW_UPDATE_AVAILABLE_TO_INSTALL
                                } else AppSettingViewModel.DEFAULT_MESSAGE_NEW_UPDATE_AVAILABLE_TO_DOWNLOAD

                            showBottomSheet(
                                title = "A new update version ${releaseNote.versionName} available!",
                                desc = CommonUtils.formatListAsBulletList(releaseNote.changes),
                                cancellable = true,
                                positiveText = "Update",
                                positiveClickCallback = {
                                    appUpdateManager.safeStartUpdateFlowForResult(
                                        result,
                                        AppUpdateType.FLEXIBLE,
                                        this@AppSettingActivity,
                                        Constants.APP_UPDATE_FLEX_REQUEST_CODE
                                    ) { e ->
                                        viewModel.setMessage(
                                            Message.ToastMessage(
                                                "Something went wrong while updating: reason:${e.message}"
                                            )
                                        )

                                    }

                                },
                                negativeText = "May be later",
                            )

                        } else {
                            viewModel.subTitleCheckForUpdate.value =
                                AppSettingViewModel.DEFAULT_MESSAGE_CHECK_FOR_UPDATE
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

            }
            InstallStatus.PENDING -> {

            }
            InstallStatus.REQUIRES_UI_INTENT -> {
                //no need to implement
            }
            InstallStatus.UNKNOWN -> {

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

}
