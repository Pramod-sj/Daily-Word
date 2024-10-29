package com.pramod.dailyword.framework.ui.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.messaging.FirebaseMessaging
import com.pramod.dailyword.BR
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.Constants
import com.pramod.dailyword.R
import com.pramod.dailyword.WOTDApp
import com.pramod.dailyword.databinding.ActivityAppSettingBinding
import com.pramod.dailyword.databinding.DialogWidgetBackgroundOpacityBinding
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.helper.restartActivity
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import com.pramod.dailyword.framework.prefmanagers.WindowAnimPrefManager
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.Message
import com.pramod.dailyword.framework.ui.common.exts.DailogHelper
import com.pramod.dailyword.framework.ui.common.exts.applyStyleOnAlertDialog
import com.pramod.dailyword.framework.ui.common.exts.openAboutPage
import com.pramod.dailyword.framework.ui.common.exts.openSplashScreen
import com.pramod.dailyword.framework.ui.common.exts.setUpToolbar
import com.pramod.dailyword.framework.ui.common.exts.showBottomSheet
import com.pramod.dailyword.framework.ui.common.exts.showCheckboxDialog
import com.pramod.dailyword.framework.ui.notification_consent.ImportantPermissionHandler
import com.pramod.dailyword.framework.ui.notification_consent.ImportantPermissionState
import com.pramod.dailyword.framework.ui.settings.custom_time_notification.NotificationAlarmScheduler
import com.pramod.dailyword.framework.ui.settings.custom_time_notification.NotificationTimePickerDialog
import com.pramod.dailyword.framework.util.CommonUtils
import com.pramod.dailyword.framework.util.safeStartUpdateFlowForResult
import com.pramod.dailyword.framework.widget.pref.Controls
import com.pramod.dailyword.framework.widget.pref.WidgetPreference
import com.pramod.dailyword.framework.widget.refreshWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

val Activity.CHECK_FOR_UPDATE_TEXT_WITH_CURRENT_VERSION: String
    get() = String.format(
        resources.getString(R.string.app_update_check_for_update_message),
        BuildConfig.VERSION_NAME
    )

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
    lateinit var widgetPrefManager: WidgetPreference

    @Inject
    lateinit var fbRemoteConfig: FBRemoteConfig

    @Inject
    lateinit var importantPermissionState: ImportantPermissionState

    @Inject
    lateinit var importantPermissionHandler: ImportantPermissionHandler

    @Inject
    lateinit var notificationAlarmScheduler: NotificationAlarmScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpToolbar(binding.toolbar, null, true)
        handleUserCase()
        initThemeValue()
        initEdgeToEdgeValue()
        initWindowAnimValue()
        initHideBadgeValue()
        bindNotificationEnabledState()
        appUpdateManager.registerListener(installStateUpdatedListener)
        setupChangeNotificationTimeDialog()

        binding.notificationDailyToggle.setOnClickListener {
            if (checkIfNotificationPermissionProvided()) {
                viewModel.notificationPrefManager.toggleDailyWordNotification()
            }
        }

        binding.notificationReminderToggle.setOnClickListener {
            if (checkIfNotificationPermissionProvided()) {
                viewModel.notificationPrefManager.toggleReminderNotification()
            }
        }

        binding.notificationMeaningToggle.setOnClickListener {
            if (checkIfNotificationPermissionProvided()) {
                viewModel.notificationPrefManager.toggleShowWordMeaningInNotification()
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notificationTriggerTimeChangeMessage.collect {
                    binding.root.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    viewModel.setMessage(Message.SnackBarMessage(it))
                }
            }
        }
    }

    private fun setupChangeNotificationTimeDialog() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.changeNotificationSubtitle.collect {
                    binding.notificationChangeTime.setSubTitle(it)
                }
            }
        }
        binding.notificationChangeTime.setOnClickListener {
            if (checkIfRequiredPermissionProvided()) {
                NotificationTimePickerDialog.show(
                    notificationTriggerTime = viewModel.notificationTriggerTime.value,
                    fragmentManager = supportFragmentManager,
                    changeNotificationCallback = {
                        viewModel.setNotificationTriggerTime(it)
                    })
            }
        }
    }

    private fun checkIfRequiredPermissionProvided(): Boolean {
        if (!importantPermissionState.isNotificationEnabled.value) {
            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.dialog_consent_notification_title))
                .setMessage(resources.getString(R.string.dialog_consent_notification_desc))
                .setPositiveButton(resources.getString(R.string.dialog_consent_notification_btn)) { _, _ ->
                    importantPermissionHandler.launchNotificationPermissionFlow()
                }.show()
            return false
        }
        if (!importantPermissionState.isBatteryOptimizationDisabled.value) {
            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.dialog_consent_disable_optimization_title))
                .setMessage(resources.getString(R.string.dialog_consent_disable_optimization_desc))
                .setPositiveButton(resources.getString(R.string.dialog_consent_disable_optimization_btn)) { _, _ ->
                    importantPermissionHandler.launchDisableBatteryOptimizationPermissionFlow()
                }.show()
            return false
        }
        if (!importantPermissionState.isSetAlarmEnabled.value) {
            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.dialog_consent_exact_alarms_title))
                .setMessage(resources.getString(R.string.dialog_consent_exact_alarms_desc))
                .setPositiveButton(resources.getString(R.string.dialog_consent_exact_alarms_btn)) { _, _ ->
                    importantPermissionHandler.launchAllowSettingAlarmPermissionFlow()
                }.show()
            return false
        }
        if (!importantPermissionState.isUnusedAppPausingDisabled.value) {
            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.dialog_consent_unused_app_title))
                .setMessage(resources.getString(R.string.dialog_consent_unused_app_desc))
                .setPositiveButton(resources.getString(R.string.dialog_consent_unused_app_btn)) { _, _ ->
                    importantPermissionHandler.launchDisableUnusedAppPaused()
                }.show()
            return false
        }
        return true
    }

    private fun checkIfNotificationPermissionProvided(): Boolean {
        if (!importantPermissionState.isNotificationEnabled.value) {
            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.dialog_consent_notification_title))
                .setMessage(resources.getString(R.string.dialog_consent_notification_desc))
                .setPositiveButton(resources.getString(R.string.dialog_consent_notification_btn)) { _, _ ->
                    importantPermissionHandler.launchNotificationPermissionFlow()
                }.show()
            return false
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnCompleteListener { appUpdateInfoTask ->
            if (appUpdateInfoTask.isSuccessful) {
                when (appUpdateInfoTask.result.updateAvailability()) {
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                        if (appUpdateInfoTask.result.installStatus() == InstallStatus.DOWNLOADED) {
                            viewModel.subTitleCheckForUpdate.value =
                                resources.getString(R.string.app_update_update_available_install_message)
                        }
                    }

                    UpdateAvailability.UPDATE_AVAILABLE -> {
                        viewModel.subTitleCheckForUpdate.value =
                            resources.getString(R.string.app_update_update_available_download_message)
                    }

                    UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                        viewModel.subTitleCheckForUpdate.value =
                            CHECK_FOR_UPDATE_TEXT_WITH_CURRENT_VERSION
                    }

                    UpdateAvailability.UNKNOWN -> {
                        viewModel.subTitleCheckForUpdate.value =
                            CHECK_FOR_UPDATE_TEXT_WITH_CURRENT_VERSION
                    }
                }
            } else {
                viewModel.subTitleCheckForUpdate.value =
                    CHECK_FOR_UPDATE_TEXT_WITH_CURRENT_VERSION
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
                    resources.getString(R.string.change_theme_dialog_title),
                    R.array.theme_options,
                    themeManager.getThemeMode(),
                    resources.getString(R.string.change_theme_apply_btn),
                    resources.getString(R.string.change_theme_cancel_btn)
                ) { selectedThemeText ->
                    themeManager.applyTheme(selectedThemeText)
                }
            }

            override fun toggleWindowAnimation() {
                windowAnimPrefManager.toggle()
                Timber.i("toggleWindowAnimation: " + edgeToEdgePrefManager.isEnabled())
            }

            override fun toggleEdgeToEdge() {
                edgeToEdgePrefManager.toggle()
                restartActivity(true)
            }

            override fun toggleBadgeVisibility() {
                prefManager.toggleHideBadge()
            }

            override fun checkForUpdate() {

                if (viewModel.subTitleCheckForUpdate.value
                    == resources.getString(R.string.app_update_update_downloading_message)
                ) {
                    return
                }

                viewModel.subTitleCheckForUpdate.value =
                    resources.getString(R.string.checking_for_update)

                appUpdateManager.appUpdateInfo.addOnCompleteListener { appUpdateInfoTask ->

                    if (appUpdateInfoTask.isSuccessful) {
                        when (appUpdateInfoTask.result.updateAvailability()) {
                            UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                                viewModel.subTitleCheckForUpdate.value =
                                    resources.getString(R.string.update_in_progress)

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
                                                String.format(
                                                    resources.getString(R.string.something_went_wrong_during_update),
                                                    e.message
                                                )
                                            )
                                        )

                                    }
                                }
                            }

                            UpdateAvailability.UPDATE_AVAILABLE -> {
                                viewModel.subTitleCheckForUpdate.value =
                                    resources.getString(R.string.app_update_update_available_download_message)
                                val releaseNote = fbRemoteConfig.getLatestRelease()
                                releaseNote?.let {
                                    showBottomSheet(
                                        title = String.format(
                                            resources.getString(R.string.new_update_available),
                                            releaseNote.versionName
                                        ),
                                        desc = CommonUtils.formatListAsBulletList(releaseNote.changes),
                                        cancellable = true,
                                        positiveText = resources.getString(R.string.update_dialog_positive_btn),
                                        positiveClickCallback = {
                                            appUpdateManager.safeStartUpdateFlowForResult(
                                                appUpdateInfoTask.result,
                                                AppUpdateType.FLEXIBLE,
                                                this@AppSettingActivity,
                                                Constants.APP_UPDATE_FLEX_REQUEST_CODE
                                            ) { e ->
                                                viewModel.setMessage(
                                                    Message.ToastMessage(
                                                        String.format(
                                                            resources.getString(R.string.something_went_wrong_during_update),
                                                            e.message
                                                        )
                                                    )
                                                )

                                            }

                                        },
                                        negativeText = resources.getString(R.string.update_dialog_negative_btn),
                                    )
                                }
                            }

                            UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                                viewModel.subTitleCheckForUpdate.value =
                                    CHECK_FOR_UPDATE_TEXT_WITH_CURRENT_VERSION
                            }

                            UpdateAvailability.UNKNOWN -> {
                                viewModel.subTitleCheckForUpdate.value =
                                    CHECK_FOR_UPDATE_TEXT_WITH_CURRENT_VERSION
                            }
                        }
                    } else {
                        viewModel.subTitleCheckForUpdate.value =
                            CHECK_FOR_UPDATE_TEXT_WITH_CURRENT_VERSION
                        viewModel.setMessage(
                            Message.ToastMessage(
                                String.format(
                                    resources.getString(R.string.error_failed_to_check_update),
                                    appUpdateInfoTask.exception?.message.toString()
                                )
                            )
                        )
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
                        viewModel.setMessage(Message.SnackBarMessage(resources.getString(R.string.token_captured_success_message)))
                    } else {
                        viewModel.setMessage(Message.SnackBarMessage(resources.getString(R.string.token_captured_failed_message)))
                    }
                }
            }

            override fun clearAppData() {
                showBottomSheet(
                    resources.getString(R.string.clear_data_dialog_title),//"Clear App Data",
                    resources.getString(R.string.clear_data_dialog_desc),//"Note: You'll loose all your bookmarks and word view data if you proceed",
                    positiveText = resources.getString(R.string.clear_data_dialog_positive_btn),//"Proceed",
                    positiveClickCallback = {
                        val cleared = WOTDApp.clearAppData(this@AppSettingActivity)
                        if (cleared) {
                            openSplashScreen(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                            )
                        }
                    },
                    negativeText = resources.getString(R.string.clear_data_dialog_negative_btn), //"Cancel",
                    negativeClickCallback = {

                    }
                )
            }

            override fun showWidgetBackgroundDialog() {
                showWidgetBgControlDialog()
            }

            override fun showWidgetControlsDialog() {
                showCheckboxDialog(
                    title = resources.getString(R.string.quick_action_dialog_title),//"Widget Controls",
                    items = Controls.entries.map { it.label },
                    selectedItems = prefManager.getVisibleWidgetControls().toList(),
                    positiveText = resources.getString(R.string.quick_action_dialog_positive),//"Apply",
                    onPositiveClickCallback = {
                        prefManager.setVisibleWidgetControls(it.toSet())
                        refreshWidget()
                    },
                    negativeText = resources.getString(R.string.quick_action_dialog_negative)
                )
            }

        }
    }

    private val installStateUpdatedListener = InstallStateUpdatedListener { installState ->
        when (installState.installStatus()) {
            InstallStatus.DOWNLOADED -> {
                viewModel.subTitleCheckForUpdate.value =
                    resources.getString(R.string.app_update_update_available_install_message)
            }

            InstallStatus.CANCELED -> {
                viewModel.subTitleCheckForUpdate.value =
                    resources.getString(R.string.app_update_update_available_download_message)
                viewModel.setMessage(Message.ToastMessage(resources.getString(R.string.error_user_cancelled_update)))
            }

            InstallStatus.DOWNLOADING -> {
                viewModel.subTitleCheckForUpdate.value =
                    resources.getString(R.string.app_update_update_downloading_message) + " " +
                            try {
                                ((installState.bytesDownloaded() * 100) / installState.totalBytesToDownload())
                            } catch (_: Exception) {
                                "0"
                            } + "%"

            }

            InstallStatus.FAILED -> {
                viewModel.setMessage(
                    Message.ToastMessage(
                        String.format(
                            resources.getString(R.string.error_update_process_failed),
                            installState.installErrorCode().toString()
                        )
                    )
                )
            }

            InstallStatus.INSTALLED -> {
                viewModel.setMessage(Message.ToastMessage(resources.getString(R.string.success_update)))
            }

            InstallStatus.INSTALLING -> {
                Toast.makeText(
                    applicationContext,
                    resources.getString(R.string.installation_started),
                    Toast.LENGTH_SHORT
                ).show()
            }

            InstallStatus.PENDING -> {}
            InstallStatus.REQUIRES_UI_INTENT -> {
                viewModel.subTitleCheckForUpdate.value =
                    resources.getString(R.string.app_update_update_available_download_message)
                //no need to implement
                Toast.makeText(
                    applicationContext,
                    resources.getString(R.string.update_ui_intent_issue),
                    Toast.LENGTH_SHORT
                ).show()
            }

            InstallStatus.UNKNOWN -> {
                viewModel.subTitleCheckForUpdate.value =
                    resources.getString(R.string.app_update_update_available_download_message)
                Toast.makeText(
                    applicationContext,
                    resources.getString(R.string.update_unknown_issue),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    fun showWidgetBgControlDialog() {
        val binding =
            DialogWidgetBackgroundOpacityBinding.inflate(LayoutInflater.from(this), null, false)
        binding.sliderWidgetBodyBgControl.value = prefManager.getWidgetBodyAlpha().toFloat()
        binding.sliderWidgetBgControl.value = prefManager.getWidgetBackgroundAlpha().toFloat()
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.background_transparency_dialog_title))
            .setView(binding.root)
            .setPositiveButton(resources.getString(R.string.background_transparency_dialog_positive)) { _, _ ->
                prefManager.setWidgetBodyAlpha(binding.sliderWidgetBodyBgControl.value.toInt())
                prefManager.setWidgetBackgroundAlpha(binding.sliderWidgetBgControl.value.toInt())
                refreshWidget()
            }
            .setNegativeButton(resources.getString(R.string.background_transparency_dialog_negative)) { _, _ ->

            }.create()
        dialog.applyStyleOnAlertDialog()
        dialog.show()
    }

    private fun bindNotificationEnabledState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                importantPermissionState
                    .isAllImportantPermissionGranted
                    .collect {
                        if (!it) {
                            binding.ivNotificationAlert.isVisible = true
                            binding.cardNotification.setOnClickListener {
                                checkIfRequiredPermissionProvided()
                            }
                            binding.cardNotification.isEnabled = true
                        } else {
                            binding.ivNotificationAlert.isVisible = false
                            binding.cardNotification.isEnabled = false
                        }
                    }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                importantPermissionState
                    .isNotificationEnabled
                    .collect {
                        binding.notificationDailyToggle.alpha = if (it) 1f else 0.5f
                        binding.notificationMeaningToggle.alpha = if (it) 1f else 0.5f
                        binding.notificationReminderToggle.alpha = if (it) 1f else 0.5f
                    }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                importantPermissionState
                    .isAllImportantPermissionGranted
                    .collect {
                        binding.notificationChangeTime.alpha = if (it) 1f else 0.5f
                    }
            }
        }
    }


}


internal fun View.setEnabled(enabled: Boolean, alphaValue: Float = 0.5f) {
    isEnabled = enabled
    alpha = if (enabled) 1f else alphaValue
}