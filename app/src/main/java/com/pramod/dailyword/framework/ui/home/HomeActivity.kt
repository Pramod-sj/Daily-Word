package com.pramod.dailyword.framework.ui.home

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.util.Pair
import android.view.HapticFeedbackConstants
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestAppUpdateInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.gson.Gson
import com.judemanutd.autostarter.AutoStartPermissionHelper
import com.pramod.dailyword.BR
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.Constants
import com.pramod.dailyword.R
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.databinding.ActivityHomeBinding
import com.pramod.dailyword.framework.firebase.FBMessageService
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.helper.NotificationHelper
import com.pramod.dailyword.framework.helper.billing.BillingHelper
import com.pramod.dailyword.framework.helper.billing.PurchaseListenerImpl
import com.pramod.dailyword.framework.helper.openWebsite
import com.pramod.dailyword.framework.helper.safeImmutableFlag
import com.pramod.dailyword.framework.prefmanagers.AutoStartPrefManager
import com.pramod.dailyword.framework.prefmanagers.HomeScreenBadgeManager
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import com.pramod.dailyword.framework.prefmanagers.WindowAnimPrefManager
import com.pramod.dailyword.framework.transition.doOnViewPreDrawn
import com.pramod.dailyword.framework.ui.changelogs.ChangelogDialogFragment
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.Message
import com.pramod.dailyword.framework.ui.common.bindingadapter.CommonBindindAdapters
import com.pramod.dailyword.framework.ui.common.exts.*
import com.pramod.dailyword.framework.ui.dialog.BottomMenuDialog
import com.pramod.dailyword.framework.ui.donate.DONATE_ITEM_LIST
import com.pramod.dailyword.framework.ui.donate.DonateBottomDialogFragment
import com.pramod.dailyword.framework.ui.splash_screen.SplashScreenActivity
import com.pramod.dailyword.framework.util.CommonUtils
import com.pramod.dailyword.framework.util.CommonUtils.formatListAsBulletList
import com.pramod.dailyword.framework.util.buildUpdateAvailableToDownloadSpannableString
import com.pramod.dailyword.framework.util.buildUpdateAvailableToInstallSpannableString
import com.pramod.dailyword.framework.util.safeStartUpdateFlowForResult
import com.pramod.dailyword.framework.widget.DailyWordWidgetProvider
import com.pramod.dailyword.framework.widget.refreshWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding, HomeViewModel>(R.layout.activity_home) {


    override val viewModel: HomeViewModel by viewModels()

    override val bindingVariable: Int = BR.mainViewModel

    @Inject
    lateinit var windowAnimPrefManager: WindowAnimPrefManager

    private val appUpdateManager: AppUpdateManager by lazy {
        AppUpdateManagerFactory.create(this)
    }

    @Inject
    lateinit var autoStartPrefManager: AutoStartPrefManager

    @Inject
    lateinit var autoStartPermissionHelper: AutoStartPermissionHelper

    @Inject
    lateinit var prefManager: PrefManager

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var homeScreenBadgeManager: HomeScreenBadgeManager

    lateinit var billingHelper: BillingHelper

    @Inject
    lateinit var fbRemoteConfig: FBRemoteConfig


    private val pastWordAdapter: PastWordAdapter by lazy {
        PastWordAdapter(onItemClickCallback = { i: Int, word: Word ->
            val view = binding.mainRecyclerviewPastWords.layoutManager!!.findViewByPosition(i)
            view?.let { nonNullView ->
                intentToWordDetail(nonNullView, word)
            } ?: intentToWordDetail(null, word)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        transparentNavBar = true
        super.onCreate(savedInstanceState)
        supportPostponeEnterTransition()
        checkForUpdate()
        loadBackgroundImage()
        initToolbar()
        initAppUpdate()
        initBillingHelper()
        showChangelogDialog()
        settingUpAudioIconTint()
        setUpViewCallbacks()
        handledeepLinkNotificationAndWidgetClick()
        //handleWidgetExtras()
        setUpRecyclerViewAdapter()
        shouldShowRatingDialog()
        handleShowingCreditAndAutoStartDialog()
        handleBadgeVisibility()
        silentRefreshWidget()
    }

    private fun initBillingHelper() {
        billingHelper = BillingHelper(
            this,
            DONATE_ITEM_LIST.map { it.itemProductId })
        billingHelper.addPurchaseListener(object : PurchaseListenerImpl() {
            override fun onBillingPurchasesProcessed() {
                super.onBillingPurchasesProcessed()
                lifecycleScope.launch {
                    prefManager.setHasDonated(billingHelper.queryPurchases().isNotEmpty())
                    shouldShowSupportDevelopmentDialog()
                }
            }

            override fun onPurchasedRestored(sku: String) {
                super.onPurchasedRestored(sku)
                prefManager.setHasDonated(true)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        pastWordAdapter.setCanStartActivity(true)
        appUpdateManager.appUpdateInfo.addOnCompleteListener { appUpdateInfo ->
            if (appUpdateInfo.isSuccessful) {
                when (appUpdateInfo.result.updateAvailability()) {
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                        fbRemoteConfig.getLatestRelease()?.let { releaseNote ->
                            if (releaseNote.isForceUpdate) {
                                appUpdateManager.safeStartUpdateFlowForResult(
                                    appUpdateInfo = appUpdateInfo.result,
                                    appUpdateType = AppUpdateType.IMMEDIATE,
                                    activity = this@HomeActivity,
                                    requestCode = Constants.APP_UPDATE_IMMEDIATE_REQUEST_CODE
                                ) { e ->
                                    viewModel.setMessage(
                                        Message.ToastMessage(
                                            "Something went wrong during update process: reason:${e.message}"
                                        )
                                    )
                                    finishAffinity()
                                }
                            }
                        }
                    }
                    UpdateAvailability.UNKNOWN -> {

                    }
                    UpdateAvailability.UPDATE_AVAILABLE -> {

                    }
                    UpdateAvailability.UPDATE_NOT_AVAILABLE -> {

                    }
                }
            }
        }
    }

    private fun loadBackgroundImage() {
        Glide.with(this)
            .load(BuildConfig.HOME_BACKGROUND_URL)
            .centerCrop()
            .into(binding.homeImageViewBuildings)
    }

    private fun initToolbar() {
        //setUpToolbar(binding.toolbar, null, false)
        viewModel.setTitle(SpannableString(CommonUtils.getGreetMessage()))
        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.setTitle(CommonUtils.getFancyAppName(this))
        }, 2000)
        binding.customToolbar.buttonToolbarNavigation.setImageResource(R.drawable.ic_vocabulary_24dp)
        binding.customToolbar.buttonToolbarOptionMenu.setImageResource(R.drawable.ic_more_vert_black_24dp)
        binding.customToolbar.buttonToolbarOptionMenu.setOnClickListener {

            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

            val bottomMenuDialog = BottomMenuDialog
                .show(supportFragmentManager)
            bottomMenuDialog.bottomMenuItemClickListener =
                object : BottomMenuDialog.BottomMenuItemClickListener {
                    override fun onMenuItemClick(menuItem: MenuItem) {
                        when (menuItem.itemId) {
                            R.id.menu_settings -> {
                                openSettingPage()
                            }
                            R.id.menu_donate -> {
                                DonateBottomDialogFragment.show(supportFragmentManager)
                            }
                            R.id.menu_share -> {
                                CommonUtils.viewToBitmap(binding.coordinatorLayout)
                                    ?.let { bitmap ->
                                        shareApp(bitmap = bitmap)
                                    } ?: shareApp()
                            }

                            R.id.menu_about -> openAboutPage()
                        }
                    }
                }
        }
        viewModel.title().observe(this) {
            CommonBindindAdapters.switchingText(binding.customToolbar.txtViewToolbarTitle, it)
        }
    }

    private fun handleBadgeVisibility() {
        homeScreenBadgeManager.showBadgeOnBookmark().observe(this) {
            binding.viewBadgeBookmark.isVisible = it
        }
        homeScreenBadgeManager.showBadgeOnWordList().observe(this) {
            binding.viewBadgeWordList.isVisible = it
        }
        homeScreenBadgeManager.showBadgeOnRandomWord().observe(this) {
            binding.viewBadgeRandomWord.isVisible = it
        }
        homeScreenBadgeManager.showBadgeOnRecap().observe(this) {
            binding.viewBadgeRecap.isVisible = it
        }
    }

    private fun settingUpAudioIconTint() {
        themeManager.liveData().observe(this) {
            binding.lottieSpeaker.post {
                binding.lottieSpeaker.changeLayersColor(R.color.app_icon_tint)
            }
        }
    }

    private fun setUpViewCallbacks() {
        viewModel.navigator = object : HomeNavigator {
            override fun copyToClipboard(word: Word?) {
                word?.word?.let {
                    CommonUtils.copyToClipboard(this@HomeActivity, it)
                }
            }

            override fun readMore(v: View?, word: Word?) {
                word?.let {
                    if (pastWordAdapter.canStart()) {
                        pastWordAdapter.setCanStartActivity(false)
                        val view = binding.mainLinearLayoutWotd
                        intentToWordDetail(view, word)
                    }
                }
            }

            override fun learnAll(v: View?) {
                openWordListPage()
            }

            override fun gotoBookmark(v: View?) {
                openBookmarksPage()
            }

            override fun gotoRecap(v: View?) {
                openRecapPage()
            }

            override fun gotoRandomWord(v: View?) {
                openRandomWordPage()
            }

        }

    }

    private fun showChangelogDialog() {
        if (prefManager.getLastSavedAppVersion() < BuildConfig.VERSION_CODE) {
            ChangelogDialogFragment.show(supportFragmentManager)
            prefManager.updateLastSavedAppVersion()
        }
    }

    private fun handleShowingCreditAndAutoStartDialog() {
        if (prefManager.shouldShowMWCreditDialog()) {
            prefManager.changeMWCreditDialogShown(false)
            showBottomSheet(
                "App Content Credit",
                resources.getString(R.string.merriam_webster_credit_text),
                positiveText = "Merriam-Webster",
                positiveClickCallback = {
                    openWebsite(resources.getString(R.string.app_merriam_webster_icon_url))
                },
                negativeText = "Close"
            ) {
                //prompt for auto start settings when credit dialog is dimissed
                promptAutoStart()
            }
        } else {
            //prompt for auto start settings when credit dialog is already shown
            promptAutoStart()
        }
    }

    private fun showNotification(word: Word) {
        val notification = notificationHelper.createNotification(
            title = "Welcome to Daily Word!",
            body = "Your first word of the day is '${word.word}'",
            cancelable = true,
            pendingIntent = PendingIntent.getActivity(
                applicationContext,
                NotificationHelper.generateUniqueNotificationId(),
                Intent(this, SplashScreenActivity::class.java).apply {
                    putExtra(
                        FBMessageService.EXTRA_NOTIFICATION_PAYLOAD,
                        Gson().toJson(
                            FBMessageService.MessagePayload(
                                date = word.date!!,
                                deepLink = FBMessageService.DEEP_LINK_TO_WORD_DETAILED
                            )
                        )
                    )
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                },
                safeImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT)
            )
        )
        notificationHelper.showNotification(
            notification = notification
        )

    }

    private fun setUpRecyclerViewAdapter() {
        binding.mainRecyclerviewPastWords.adapter = pastWordAdapter
        viewModel.wordOfTheDay.observe(this, {
            it?.let {
                if (!it.isSeen) {
                    viewModel.updateWordSeenStatus(it)
                    if (PrefManager.getInstance(this).getAppLaunchCount() == 1
                        && !viewModel.firstNotificationShown
                    ) {
                        viewModel.firstNotificationShown = true
                        showNotification(it)
                    }
                }
            }
        })

        viewModel.last6DayWords.observe(this, {
            pastWordAdapter.submitList(it)
        })
    }

    private fun intentToWordDetail(view: View? = null, word: Word) {
        val option = view?.let {
            ActivityOptions.makeSceneTransitionAnimation(
                this,
                Pair(it, word.date!!)
            )
        }

        word.date?.let { date ->

            openWordDetailsPage(
                wordDate = date,
                option = option,
                shouldAnimate = windowAnimPrefManager.isEnabled(),
                word = word
            )

        }
    }

    private fun shouldShowRatingDialog() {
        if (prefManager.shouldShowRateNowDialog()) {

            val manager = ReviewManagerFactory.create(this)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener {
                if (it.isSuccessful) {
                    val requestInfo = it.result
                    val reviewFlow = manager.launchReviewFlow(this, requestInfo)
                    reviewFlow.addOnCompleteListener { reviewFlowResult ->
                        if (reviewFlowResult.isSuccessful) {
                            Timber.i("shouldShowRatingDialog: succeed: done")
                        } else {
                            Timber.i(

                                "shouldShowRatingDialog: failed:" + reviewFlowResult.exception.toString()
                            )
                        }
                    }

                } else {
                    Timber.i("shouldShowRatingDialog: failed:" + it.exception.toString())
                }
            }

        }

    }

    private fun initAppUpdate() {
        binding.btnUpdateBtn.setOnClickListener {
            lifecycleScope.launch {
                if (appUpdateManager.requestAppUpdateInfo()
                        .installStatus() == InstallStatus.DOWNLOADED
                ) appUpdateManager.completeUpdate()
                else checkForUpdate()
            }
        }
    }

    private fun promptAutoStart() {
        if (autoStartPermissionHelper.isAutoStartPermissionAvailable(this)) {
            if (!autoStartPrefManager.isAutoStartAlreadyEnabled()) {
                showBasicDialog(
                    title = "Auto Start",
                    message =
                    if (!autoStartPrefManager.isClickedOnSetting())
                        "Please enable auto start else notification feature won't work properly!"
                    else
                        "It's look like you have already went to setting, if you have enabled AutoStart clicked on 'Already Enabled'",
                    positiveText = "Setting",
                    positiveClickCallback = {
                        if (!autoStartPermissionHelper.getAutoStartPermission(this)) {
                            viewModel.setMessage(Message.SnackBarMessage("Sorry we unable to locate auto start setting, Please enable it manually."))
                        }
                        autoStartPrefManager.clickedOnSetting()
                    },
                    negativeText = "Cancel",
                    negativeClickCallback = {

                    },
                    neutralText = "Already Enabled",
                    neutralClickCallback = {
                        autoStartPrefManager.clickedOnAlreadyEnabled()
                    }
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.i("onActivityResult: $requestCode:$resultCode")
        if (requestCode == Constants.APP_UPDATE_IMMEDIATE_REQUEST_CODE) {
            //when user clicks update button

            Timber.i("onActivityResult: $resultCode")
            when (resultCode) {
                RESULT_OK -> {

                    Toast.makeText(
                        applicationContext,
                        "App download starts...",
                        Toast.LENGTH_LONG
                    ).show()

                }
                RESULT_CANCELED -> {
                    //if you want to request the update again just call checkUpdate()
                    Toast.makeText(
                        applicationContext,
                        "Sorry you can't use Daily Word app unless you update!",
                        Toast.LENGTH_LONG
                    ).show()
                    finishAffinity()
                }
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                    Toast.makeText(
                        applicationContext,
                        "App download failed, we're trying again for you",
                        Toast.LENGTH_LONG
                    ).show()

                    checkForUpdate()
                }
            }
        }
    }

    private fun handledeepLinkNotificationAndWidgetClick() {
        val messagePayload: FBMessageService.MessagePayload? =
            Gson().fromJson(
                intent.extras?.getString(FBMessageService.EXTRA_NOTIFICATION_PAYLOAD),
                FBMessageService.MessagePayload::class.java
            )
        Timber.i(
            "deepLinkNotification: ${intent.extras?.getString(FBMessageService.EXTRA_NOTIFICATION_PAYLOAD)}"
        )

        val widgetClickWordDate =
            intent.extras?.getString(DailyWordWidgetProvider.EXTRA_INTENT_TO_HOME_WORD_DATE)
        if (widgetClickWordDate != null) {
            openWordDetailsPage(widgetClickWordDate, option = null)
        } else
            when (messagePayload?.deepLink) {
                FBMessageService.DEEP_LINK_TO_WORD_DETAILED -> {
                    openWordDetailsPage(messagePayload.date, option = null)
                }
                FBMessageService.DEEP_LINK_TO_WORD_LIST -> {
                    openWordListPage()
                }
                else -> {

                }
            }
    }

    private fun handleWidgetExtras() {
        val date = intent.extras?.getString(DailyWordWidgetProvider.EXTRA_INTENT_TO_HOME_WORD_DATE)
        date?.let {
            openWordDetailsPage(it, null)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        val widgetClickWordDate =
            intent?.extras?.getString(DailyWordWidgetProvider.EXTRA_INTENT_TO_HOME_WORD_DATE)

        Timber.i("onNewIntent: " + widgetClickWordDate)
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)

        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())

        supportPostponeEnterTransition()
        doOnViewPreDrawn(binding.mainRecyclerviewPastWords) {
            supportStartPostponedEnterTransition()
        }

    }

    private fun checkForUpdate() {
        appUpdateManager.registerListener(installStateUpdatedListener)
        appUpdateManager.appUpdateInfo.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val releaseNote = fbRemoteConfig.getLatestRelease()
                if (releaseNote != null) {
                    when (task.result.updateAvailability()) {
                        UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                            if (task.result.installStatus() == InstallStatus.DOWNLOADED) {
                                viewModel.setAppUpdateMessage(
                                    buildUpdateAvailableToInstallSpannableString(releaseNote)
                                )
                                viewModel.setAppUpdateButtonText("Install")
                                viewModel.setAppUpdateDownloadProgress(100)
                            }
                        }
                        UpdateAvailability.UNKNOWN -> {}
                        UpdateAvailability.UPDATE_AVAILABLE -> {
                            viewModel.setAppUpdateMessage(
                                buildUpdateAvailableToDownloadSpannableString(releaseNote)
                            )
                            viewModel.setAppUpdateDownloadProgress(0)
                            viewModel.setAppUpdateButtonText("Update")
                            if (releaseNote.isForceUpdate) {
                                showBottomSheet(
                                    title = "A new update version ${releaseNote.versionName} is available!",
                                    desc = formatListAsBulletList(releaseNote.changes),
                                    cancellable = false,
                                    positiveText = "Update",
                                    positiveClickCallback = {
                                        appUpdateManager.safeStartUpdateFlowForResult(
                                            task.result,
                                            AppUpdateType.IMMEDIATE,
                                            this@HomeActivity,
                                            Constants.APP_UPDATE_IMMEDIATE_REQUEST_CODE
                                        ) { e ->
                                            viewModel.setMessage(
                                                Message.ToastMessage(
                                                    "Something went wrong during update process: reason:${e.message}"
                                                )
                                            )
                                            finishAffinity()
                                        }

                                    },
                                    negativeText = "Close App",
                                    negativeClickCallback = {
                                        Toast.makeText(
                                            applicationContext,
                                            "Sorry but you need to update app",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        finishAffinity()
                                    },
                                )
                            } else {
                                showBottomSheet(
                                    title = "A new update version ${releaseNote.versionName} is available!",
                                    desc = formatListAsBulletList(releaseNote.changes),
                                    cancellable = true,
                                    positiveText = "Update",
                                    positiveClickCallback = {
                                        appUpdateManager.safeStartUpdateFlowForResult(
                                            task.result,
                                            AppUpdateType.FLEXIBLE,
                                            this@HomeActivity,
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
                            Timber.i("checkForUpdate: no update available")
                        }
                    }
                } else {
                    viewModel.setAppUpdateModel(null)
                }
            } else {
                viewModel.setMessage(Message.ToastMessage("Failed to check update:" + task.exception?.message))
            }

        }
    }

    private val installStateUpdatedListener = InstallStateUpdatedListener { installState ->
        when (installState.installStatus()) {
            InstallStatus.DOWNLOADED -> {
                Timber.i(": DOWNLOADED")
                fbRemoteConfig.getLatestRelease()?.let {
                    viewModel.setAppUpdateMessage(buildUpdateAvailableToInstallSpannableString(it))
                    viewModel.setAppUpdateDownloadProgress(100)
                    viewModel.setAppUpdateButtonText("Install")
                }
            }
            InstallStatus.CANCELED -> {
                Timber.i(": CANCELED")
                viewModel.setAppUpdateDownloadProgress(0)
                viewModel.setAppUpdateButtonText("Update")
                viewModel.setMessage(Message.ToastMessage("User cancelled update app process"))
            }
            InstallStatus.DOWNLOADING -> {
                val downloadPercentage =
                    ((installState.bytesDownloaded() * 100) / installState.totalBytesToDownload()).toInt()
                viewModel.setAppUpdateButtonText("$downloadPercentage%")
                viewModel.setAppUpdateDownloadProgress(downloadPercentage)
            }
            InstallStatus.FAILED -> {
                Timber.i(": FAILED")
                viewModel.setAppUpdateDownloadProgress(0)
                viewModel.setAppUpdateButtonText("Update")
                viewModel.setMessage(Message.ToastMessage("Update process failed! reason:${installState.installErrorCode()}"))
            }
            InstallStatus.INSTALLED -> {
                Timber.i(": INSTALLED")
                viewModel.setAppUpdateDownloadProgress(0)
                viewModel.setMessage(Message.ToastMessage("Successfully updated!"))
                Toast.makeText(
                    applicationContext,
                    "Successfully install new update!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            InstallStatus.INSTALLING -> {
                viewModel.setAppUpdateDownloadProgress(0)
                Timber.i(": INSTALLING")
                Toast.makeText(
                    applicationContext,
                    "Installation started!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            InstallStatus.PENDING -> {}
            InstallStatus.REQUIRES_UI_INTENT -> {
                viewModel.setAppUpdateDownloadProgress(0)
                Timber.i(": REQUIRES_UI_INTENT")
                //no need to implement
                Toast.makeText(
                    applicationContext,
                    "UI Intent issue!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            InstallStatus.UNKNOWN -> {
                viewModel.setAppUpdateDownloadProgress(0)
                Timber.i(": UNKNOWN")
                Toast.makeText(
                    applicationContext,
                    "Unknown issue!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun silentRefreshWidget() {
        val shouldSilentRefresh =
            this.intent.extras?.getString(DailyWordWidgetProvider.EXTRA_INTENT_TO_HOME_WORD_DATE) == null
        if (shouldSilentRefresh) refreshWidget()
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }


    companion object {

        val TAG = HomeActivity::class.simpleName

    }
}