package com.pramod.dailyword.framework.ui.home

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.util.Log
import android.util.Pair
import android.view.*
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.paging.ExperimentalPagingApi
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.google.android.play.core.appupdate.AppUpdateInfo
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
import com.pramod.dailyword.framework.prefmanagers.AutoStartPrefManager
import com.pramod.dailyword.framework.prefmanagers.HomeScreenBadgeManager
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import com.pramod.dailyword.framework.prefmanagers.WindowAnimPrefManager
import com.pramod.dailyword.framework.transition.isViewsPreDrawn
import com.pramod.dailyword.framework.ui.changelogs.ChangelogDialogFragment
import com.pramod.dailyword.framework.ui.common.*
import com.pramod.dailyword.framework.ui.common.bindingadapter.CommonBindindAdapters
import com.pramod.dailyword.framework.ui.common.exts.*
import com.pramod.dailyword.framework.ui.dialog.BottomMenuDialog
import com.pramod.dailyword.framework.ui.donate.DonateBottomDialogFragment
import com.pramod.dailyword.framework.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Inject
    lateinit var appUpdateHelper: AppUpdateHelper

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

    private val pastWordAdapter: PastWordAdapter by lazy {
        PastWordAdapter(onItemClickCallback = { i: Int, word: Word ->
            val view = binding.mainRecyclerviewPastWords.layoutManager!!.findViewByPosition(i)
            view?.let { nonNullView ->
                intentToWordDetail(nonNullView, word)
            } ?: intentToWordDetail(null, word)
        })
    }

    private var bottomSheetDialog: BottomSheetDialog? = null

    private var donateBottomDialogFragment: DonateBottomDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        transparentNavBar = true
        super.onCreate(savedInstanceState)
        supportPostponeEnterTransition()
        loadBackgroundImage()
        initToolbar()
        initAppUpdate()
        showChangelogActivity()
        settingUpAudioIconTint()
        setUpViewCallbacks()
        deepLinkNotification()
        initBottomSheetMenu()
        setUpRecyclerViewAdapter()
        shouldShowRatingDialog()
        handleShowingCreditAndAutoStartDialog()
        handleBadgeVisibility()
    }

    override fun onResume() {
        super.onResume()
        pastWordAdapter.setCanStartActivity(true)
    }

    private fun loadBackgroundImage() {
        Log.i(TAG, "loadBackgroundImage: ")
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

    private fun showChangelogActivity() {
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
                positiveText = "Go to Merriam-Webster",
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
        appUpdateHelper = AppUpdateHelper(this)
        appUpdateHelper.checkForUpdate(object : AppUpdateHelper.AppUpdateAvailabilityListener {
            override fun onUpdateAvailable(appUpdateInfo: AppUpdateInfo) {
                Log.i("HomeActivity", "Update available")
                appUpdateHelper.startImmediateUpdate(appUpdateInfo) {
                    viewModel.setMessage(Message.SnackBarMessage(it))
                }
            }

            override fun onUpdateNotAvailable() {
                Log.i("HomeActivity", "You're up to date!")
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
        donateBottomDialogFragment?.onActivityResult(requestCode, resultCode, data)
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    private fun initBottomSheetMenu() {
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
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
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

                /*item.actionView?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                bottomSheetDialog?.show()*/
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)

        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())

        supportPostponeEnterTransition()
        isViewsPreDrawn(binding.mainRecyclerviewPastWords) {
            supportStartPostponedEnterTransition()
        }

    }


    companion object {

        val TAG = HomeActivity::class.simpleName

    }
}