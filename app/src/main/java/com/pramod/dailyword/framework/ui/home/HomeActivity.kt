package com.pramod.dailyword.framework.ui.home

import android.app.Activity
import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.util.Log
import android.view.*
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.asLiveData
import androidx.paging.ExperimentalPagingApi
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.gson.Gson
import com.judemanutd.autostarter.AutoStartPermissionHelper
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.databinding.ActivityHomeBinding
import com.pramod.dailyword.framework.firebase.FBMessageService
import com.pramod.dailyword.framework.helper.*
import com.pramod.dailyword.framework.prefmanagers.AutoStartPrefManager
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import com.pramod.dailyword.framework.transition.isViewsPreDrawn
import com.pramod.dailyword.framework.ui.common.*
import com.pramod.dailyword.framework.ui.common.exts.*
import com.pramod.dailyword.framework.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*


@AndroidEntryPoint
@ExperimentalCoroutinesApi
@ExperimentalPagingApi
class HomeActivity : BaseActivity<ActivityHomeBinding, HomeViewModel>() {

    override val layoutId: Int = R.layout.activity_home
    override val viewModel: HomeViewModel by viewModels()
    override val bindingVariable: Int = BR.mainViewModel

    private lateinit var pastWordAdapter: PastWordAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //initExitTransition()
        //addGradientToAppIcon()
        setUpViewCallbacks()
        deepLinkNotification()
        showChangelogActivity()
        initAppUpdate()
        initToolbar()
        initBottomSheetMenu()
        setUpRecyclerViewAdapter()
        shouldShowRatingDialog()
        //showDummyLotttieDialog()
        handleShowingCreditAndAutoStartDialog()
        handleRippleAnimationForAudioEffect()
        handleBadgeVisibility()
    }

    override fun onResume() {
        super.onResume()
        pastWordAdapter.setCanStartActivity(true)
    }

    private fun initExitTransition() {
        window.exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    private fun handleBadgeVisibility() {
        viewModel.homeScreenBadgeManager.showBadgeOnBookmark().observe(this) {
            Log.i(TAG, "handleBadgeVisibility: showBadgeOnBookmark:$it")
            binding.viewBadgeBookmark.isVisible = it
        }
        viewModel.homeScreenBadgeManager.showBadgeOnWordList().observe(this) {
            Log.i(TAG, "handleBadgeVisibility: showBadgeOnWordList:$it")
            binding.viewBadgeWordList.isVisible = it
        }
    }

    private fun handleRippleAnimationForAudioEffect() {

        themeManager.liveData().observe(this) {
            binding.lottieSpeaker.post {
                binding.lottieSpeaker.changeLayersColor(R.color.app_icon_tint)
            }
        }
    }

    private fun setUpViewCallbacks() {
        mViewModel.navigator = object : HomeNavigator {
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
                        intentToWordDetail(this@HomeActivity, view, word)
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
        mViewModel.showChangelogActivity
            .asLiveData(Dispatchers.IO)
            .observe(this) { show ->
                if (show) {
                    openChangelogPage(true)
                }
            }

    }

    private fun handleShowingCreditAndAutoStartDialog() {
        val prefManager = PrefManager.getInstance(this)

        if (prefManager.getShowInitailCreditDialogStatus()) {
            prefManager.changeShowInitialCreditDialogStatus(false)
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

    private fun initToolbar() {


        setSupportActionBar(binding.toolbar)

        supportActionBar?.let { actionBar ->


            actionBar.title = null

            /*   actionBar.setDisplayShowTitleEnabled(false)
               actionBar.setDisplayShowCustomEnabled(true)

               val layoutCustomTitleToolbarBinding: LayoutCustomTitleToolbarBinding =
                   DataBindingUtil.inflate(
                       layoutInflater,
                       R.layout.layout_custom_title_toolbar,
                       null,
                       false
                   )
               val params: ActionBar.LayoutParams = ActionBar.LayoutParams(
                   ActionBar.LayoutParams.WRAP_CONTENT,
                   ActionBar.LayoutParams.MATCH_PARENT,
                   Gravity.CENTER
               )
               actionBar.setCustomView(
                   layoutCustomTitleToolbarBinding.txtViewToolbarTitle,
                   params
               )

               mViewModel.title().observe(this) {
                   CommonBindindAdapters.switchingText(
                       layoutCustomTitleToolbarBinding.txtViewToolbarTitle,
                       it
                   )
               }*/

        }



        mViewModel.setTitle(SpannableString(CommonUtils.getGreetMessage()))
        Handler(Looper.getMainLooper()).postDelayed({
            mViewModel.setTitle(CommonUtils.getFancyAppName(this))
        }, 2000)

    }


    private fun showNotification(word: Word) {
        val notificationHelper = NotificationHelper(this@HomeActivity)
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
        pastWordAdapter = PastWordAdapter { i: Int, word: Word ->
            val view = binding.mainRecyclerviewPastWords.layoutManager!!.findViewByPosition(i)
            view?.let { nonNullView ->
                intentToWordDetail(this, nonNullView, word)
            } ?: intentToWordDetail(activity = this, null, word)

        }
        binding.mainRecyclerviewPastWords.adapter = pastWordAdapter
        viewModel.wordOfTheDayLiveData.observe(this, {
            it?.let {
                if (!it.isSeen) {
                    viewModel.updateWordSeenStatus(it)
                    if (PrefManager.getInstance(this).getAppLaunchCount() == 1
                        && !mViewModel.firstNotificationShown
                    ) {
                        mViewModel.firstNotificationShown = true
                        showNotification(it)
                    }
                }
            }
        })
        viewModel.wordsExceptTodayLiveData.observe(this, {
            pastWordAdapter.submitList(it)
        })
    }

    private fun intentToWordDetail(activity: Activity, view: View? = null, word: Word) {

        val option = view?.let {
            ActivityOptions.makeSceneTransitionAnimation(
                activity,
                view,
                resources.getString(R.string.card_transition_name)
            )
        }
        openWordDetailsPage(
            word.date!!,
            option,
            windowAnimPrefManager.isEnabled()
        )
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_more -> {
                item.actionView?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                bottomSheetDialog?.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /*   private lateinit var bottomSheetDialog: BottomSheetDialog
       private fun initBottomMenu() {
           val navigationView = NavigationView(this)
           navigationView.inflateMenu(R.menu.home_more_menu)
           navigationView.setNavigationItemSelectedListener { item ->
               when (item.itemId) {
                   R.id.menu_rate -> {
                       val intent = Intent(
                           Intent.ACTION_VIEW,
                           Uri.parse("market://details?id=$packageName")
                       )
                       startActivity(intent)
                   }
               }
               bottomSheetDialog.dismiss()
               false
           }
           bottomSheetDialog = BottomSheetDialog(this@HomeActivity)
           bottomSheetDialog.setContentView(navigationView)
       }
   */
    private fun shouldShowRatingDialog() {
        if (PrefManager.getInstance(this)
                .shouldShowRateNowDialog()
        ) {

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

            /*showStaticPageDialog(
                R.layout.dialog_rate_app,
                positiveText = "Rate Now",
                positiveClickCallback = {
                    PrefManager.getInstance(this)
                        .setUserClickedRateNow()
                    openGoogleReviewPage()
                },
                negativeText = "Later",
                neutralText = "Never",
                neutralClickCallback = {
                    PrefManager.getInstance(this)
                        .setUserClickedNever()
                }
            )*/
        }

    }

    var appUpdateHelper: AppUpdateHelper? = null
    private fun initAppUpdate() {
        appUpdateHelper = AppUpdateHelper(this)
        appUpdateHelper?.checkForUpdate(object : AppUpdateHelper.AppUpdateAvailabilityListener {
            override fun onUpdateAvailable(appUpdateInfo: AppUpdateInfo) {
                Log.i("HomeActivity", "Update available")
                appUpdateHelper?.startImmediateUpdate(appUpdateInfo) {
                    viewModel.setMessage(Message.SnackBarMessage(it))
                }
            }

            override fun onUpdateNotAvailable() {
                Log.i("HomeActivity", "You're up to date!")
            }
        })
    }

    private fun promptAutoStart() {
        val autoStartPermissionHelper = AutoStartPermissionHelper.getInstance()
        val autoStartPrefManager = AutoStartPrefManager.newInstance(this)
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
                            mViewModel.setMessage(Message.SnackBarMessage("Sorry we unable to locate auto start setting, Please enable it manually :)"))
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
        appUpdateHelper?.onActivityResult(requestCode, resultCode, data)
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


    private var bottomSheetDialog: BottomSheetDialog? = null
    private fun initBottomSheetMenu() {
        bottomSheetDialog = BottomSheetDialog(this, R.style.AppTheme_BottomSheetDialog)
        val navigationView =
            NavigationView(this)
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
                    openDonatePage()
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

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)

        window.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)

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