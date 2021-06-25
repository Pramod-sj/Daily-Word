package com.pramod.dailyword.framework.ui.home

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.util.Log
import android.util.Pair
import android.view.*
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import com.bumptech.glide.Glide
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.gson.Gson
import com.judemanutd.autostarter.AutoStartPermissionHelper
import com.pramod.dailyword.BR
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.R
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.databinding.ActivityHomeBinding
import com.pramod.dailyword.framework.firebase.FBMessageService
import com.pramod.dailyword.framework.helper.*
import com.pramod.dailyword.framework.helper.billing.BillingHelper
import com.pramod.dailyword.framework.helper.billing.PurchaseListenerImpl
import com.pramod.dailyword.framework.prefmanagers.AutoStartPrefManager
import com.pramod.dailyword.framework.prefmanagers.HomeScreenBadgeManager
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import com.pramod.dailyword.framework.prefmanagers.WindowAnimPrefManager
import com.pramod.dailyword.framework.transition.doOnViewPreDrawn
import com.pramod.dailyword.framework.ui.changelogs.ChangelogDialogFragment
import com.pramod.dailyword.framework.ui.common.*
import com.pramod.dailyword.framework.ui.common.bindingadapter.CommonBindindAdapters
import com.pramod.dailyword.framework.ui.common.exts.*
import com.pramod.dailyword.framework.ui.dialog.BottomMenuDialog
import com.pramod.dailyword.framework.ui.donate.DONATE_ITEM_LIST
import com.pramod.dailyword.framework.ui.donate.DonateBottomDialogFragment
import com.pramod.dailyword.framework.util.*
import com.pramod.dailyword.framework.widget.BaseWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
@ExperimentalCoroutinesApi
@ExperimentalPagingApi
class HomeActivity : BaseActivity<ActivityHomeBinding, HomeViewModel>(R.layout.activity_home) {


    override val viewModel: HomeViewModel by viewModels()

    override val bindingVariable: Int = BR.mainViewModel

    @Inject
    lateinit var windowAnimPrefManager: WindowAnimPrefManager

    private val appUpdateHelper: AppUpdateHelperNew by lazy {
        AppUpdateHelperNew(this, lifecycle)
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
        loadBackgroundImage()
        initToolbar()
        initAppUpdate()
        initBillingHelper()
        showChangelogDialog()
        settingUpAudioIconTint()
        setUpViewCallbacks()
        deepLinkNotification()
        //handleWidgetExtras()
        setUpRecyclerViewAdapter()
        shouldShowRatingDialog()
        handleShowingCreditAndAutoStartDialog()
        handleBadgeVisibility()
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
                Intent(this, HomeActivity::class.java).apply {
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
                PendingIntent.FLAG_UPDATE_CURRENT
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
        openWordDetailsPage(
            word.date!!,
            option,
            windowAnimPrefManager.isEnabled()
        )
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
                            Log.i(TAG, "shouldShowRatingDialog: succeed: done")
                        } else {
                            Log.i(
                                TAG,
                                "shouldShowRatingDialog: failed:" + reviewFlowResult.exception.toString()
                            )
                        }
                    }

                } else {
                    Log.i(TAG, "shouldShowRatingDialog: failed:" + it.exception.toString())
                }
            }

        }

    }

    private fun initAppUpdate() {

        appUpdateHelper.checkForUpdate(object : AppUpdateHelperNew.CheckingUpdateListener {
            override fun onUpdateAvailable(latestVersionCode: Long, isUpdateDownloaded: Boolean) {
                appUpdateHelper.showFlexibleDialog()
            }

            override fun onUpdateNotAvailable() {
                Log.i("HomeActivity", "You're up to date!")
            }

            override fun onFailed(message: String?) {
                Log.i(TAG, "onFailed: $message")
            }

        })

        appUpdateHelper.setInstallStatusListener(object :
            AppUpdateHelperNew.InstallStatusListener() {

            override fun onDownloaded() {
                super.onDownloaded()
                Toast.makeText(
                    applicationContext,
                    "Update downloaded successfully",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onInstalled() {
                super.onInstalled()
                Toast.makeText(
                    applicationContext,
                    "Successfully installed new updated",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onFailed() {
                super.onFailed()
                viewModel.setMessage(
                    Message.SnackBarMessage(
                        message = "Installation failed try again",
                        action = Action(
                            name = "Retry",
                            callback = {
                                appUpdateHelper.startInstallationProcess()
                            })
                    )
                )
            }
        })

        appUpdateHelper.setOnAppUpdateActivityResultListener(object :
            AppUpdateHelperNew.OnAppUpdateActivityResultListener {
            override fun onUserApproval() {

            }

            override fun onUserCancelled() {
                viewModel.setMessage(
                    Message.SnackBarMessage(
                        message = "Update was cancelled"
                    )
                )
            }

            override fun onUpdateFailure() {
                viewModel.setMessage(
                    Message.SnackBarMessage(
                        message = "Something went wrong while updating! Please try again."
                    )
                )
            }

        })
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
                        "It's look like you have went to setting, if you have enabled AutoStart clicked on 'Already Enabled'",
                    positiveText = "Setting",
                    positiveClickCallback = {
                        if (!autoStartPermissionHelper.getAutoStartPermission(this)) {
                            viewModel.setMessage(Message.SnackBarMessage("Sorry we unable to locate auto start setting, Please enable it manually :)"))
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
        appUpdateHelper.onActivityResult(requestCode, resultCode, data)

        Log.i(TAG, "onActivityResult: $requestCode:$resultCode")
        //donateBottomDialogFragment?.onActivityResult(requestCode, resultCode, data)
    }

    private fun deepLinkNotification() {
        val messagePayload: FBMessageService.MessagePayload? =
            Gson().fromJson(
                intent.extras?.getString(FBMessageService.EXTRA_NOTIFICATION_PAYLOAD),
                FBMessageService.MessagePayload::class.java
            )
        Log.i(
            TAG,
            "deepLinkNotification: ${intent.extras?.getString(FBMessageService.EXTRA_NOTIFICATION_PAYLOAD)}"
        )
        when (messagePayload?.deepLink) {
            FBMessageService.DEEP_LINK_TO_WORD_DETAILED -> {
                openWordDetailsPage(messagePayload.date, null)
            }
            FBMessageService.DEEP_LINK_TO_WORD_LIST -> {
                openWordListPage()
            }
            else -> {

            }
        }
    }

    private fun handleWidgetExtras() {
        val date = intent.extras?.getString(BaseWidgetProvider.EXTRA_INTENT_TO_HOME_WORD_DATE)
        date?.let {
            openWordDetailsPage(it, null)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    /*private fun initBottomSheetMenu() {
        bottomSheetDialog = BottomSheetDialog(this, R.style.AppTheme_BottomSheetDialog)
        val navigationView =
            NavigationView(this, null, R.style.NavigationItemNoRipple)
        navigationView.updatePadding(top = 10, bottom = 15)
        navigationView.overScrollMode = View.OVER_SCROLL_NEVER
        navigationView.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.transparent))
        navigationView.elevation = 0f
        navigationView.inflateMenu(R.menu.home_more_menu)
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_settings -> {
                    openSettingPage()
                }
                R.id.menu_donate -> {
                    donateBottomDialogFragment = DonateBottomDialogFragment()
                    donateBottomDialogFragment?.show(
                        supportFragmentManager,
                        DonateBottomDialogFragment.TAG
                    )
                }
                R.id.menu_share -> {
                    CommonUtils.viewToBitmap(binding.coordinatorLayout)?.let { bitmap ->
                        shareApp(bitmap = bitmap)
                    } ?: shareApp()
                }
            }
            bottomSheetDialog?.dismiss()
            false
        }
        val l = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        bottomSheetDialog?.setContentView(navigationView, l)
    }*/

    /*override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_more -> {
                item.actionView?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
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
                                    donateBottomDialogFragment = DonateBottomDialogFragment()
                                    donateBottomDialogFragment?.show(
                                        supportFragmentManager,
                                        DonateBottomDialogFragment.TAG
                                    )
                                }
                                R.id.menu_share -> {
                                    CommonUtils.viewToBitmap(binding.coordinatorLayout)
                                        ?.let { bitmap ->
                                            shareApp(bitmap = bitmap)
                                        } ?: shareApp()
                                }
                            }
                        }
                    }

                *//*item.actionView?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                bottomSheetDialog?.show()*//*
            }
        }
        return super.onOptionsItemSelected(item)
    }*/

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)

        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())

        supportPostponeEnterTransition()
        doOnViewPreDrawn(binding.mainRecyclerviewPastWords) {
            supportStartPostponedEnterTransition()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        billingHelper.close()
    }


    companion object {

        val TAG = HomeActivity::class.simpleName

    }
}