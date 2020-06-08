package com.pramod.todaysword.ui.home

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.transition.Fade
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.gson.Gson
import com.pramod.todaysword.BR
import com.pramod.todaysword.R
import com.pramod.todaysword.SnackbarMessage
import com.pramod.todaysword.databinding.ActivityMainBinding
import com.pramod.todaysword.db.model.WordOfTheDay
import com.pramod.todaysword.helper.*
import com.pramod.todaysword.ui.BaseActivity
import com.pramod.todaysword.ui.settings.AppSettingActivity
import com.pramod.todaysword.ui.word_details.WordDetailedActivity
import com.pramod.todaysword.ui.words.WordListActivity
import com.pramod.todaysword.worker.DailyWordWorker
import com.pramod.todaysword.worker.NewWordReminderWorker
import java.util.*


class HomeActivity : BaseActivity<ActivityMainBinding, HomeViewModel>() {


    companion object {
        @JvmStatic
        fun openActivity(context: Context) {
            val intent = Intent(context, HomeActivity::class.java)
            context.startActivity(intent)
        }

        @JvmStatic
        fun openActivityWithTransition(context: Context, option: ActivityOptions) {
            val intent = Intent(context, HomeActivity::class.java)
            ActivityCompat.startActivity(context, intent, option.toBundle())
        }
    }

    private lateinit var pastWordAdapter: PastWordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        initEnterTransition()
        initExitTransition()
        super.onCreate(savedInstanceState)
        initAppUpdate()
        initToolbar()
        initBottomMenu()
        setUpRecyclerViewAdapter()
        setObservers()
        initLearnAllEvent()
        shouldShowRatingDialog()
        //edgeToEdgeSettingChanged()
        arrangeViewsAccordingToEdgeToEdge()
        //showDummyNotification()
    }


    private fun arrangeViewsAccordingToEdgeToEdge() {
        if (WindowPreferencesManager.newInstance(this).isEdgeToEdgeEnabled()) {
            ViewCompat.setOnApplyWindowInsetsListener(
                mBinding.root
            ) { v, insets ->
                mBinding.appBar.setPadding(
                    0, insets.systemWindowInsetTop, 0, 0
                )

                val paddingBottom = insets.systemWindowInsetBottom

                mBinding.homeImageViewBuildings.setPadding(
                    0,
                    0,
                    0,
                    paddingBottom
                )
                insets
            };
        }
    }

    private fun initToolbar() {
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.let { title = null }
    }

    private fun initEnterTransition() {
        val fade = Fade()
        fade.excludeTarget(android.R.id.navigationBarBackground, true)
        fade.excludeTarget(android.R.id.statusBarBackground, true)
        fade.duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        window.enterTransition = fade
    }

    private fun initExitTransition() {
        /*val fade = Fade()
        fade.duration = 150
        fade.excludeTarget(android.R.id.navigationBarBackground, true)
        fade.excludeTarget(android.R.id.statusBarBackground, true)
        window.exitTransition = fade*/
        window.sharedElementsUseOverlay = false;
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
    }

    private fun showDummyNotification() {
        val notificationHelper = NotificationHelper(this@HomeActivity)
        val notification = notificationHelper.createNotification(
            title = "Here's your Today's Word",
            body = "Hello world", cancelable = true
        )
        notificationHelper.makeNotification(
            notification = notification
        )
    }

    private fun setUpRecyclerViewAdapter() {
        pastWordAdapter = PastWordAdapter { i: Int, wordOfTheDay: WordOfTheDay ->
            getViewModel().navigateToWordDetailed(SelectedItem.init(i, wordOfTheDay))
        }
        mBinding.pastWordAdapter = pastWordAdapter
    }

    private fun initLearnAllEvent() {
        mViewModel.getLearnAllLiveData().observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                WordListActivity.openActivity(this)
            }
        })
    }

    private fun setObservers() {
        mViewModel.getTodaysWordOfTheDay().observe(this, Observer {
            it?.let {
                if (!it.isSeen) {
                    it.isSeen = true
                    it.seenAtTimeInMillis = Calendar.getInstance().timeInMillis
                    mViewModel.updateWordSeenStatus(it)
                }
            }
        })
        mViewModel.getWordsExceptToday().observe(this, Observer {
            it?.let { words ->
                pastWordAdapter.setWords(words)
            }
        })
        mViewModel.observeNavigateToWordDetailedEvent().observe(this, Observer {
            val selectedItemEvent = it.getContentIfNotHandled()
            selectedItemEvent?.let { selectedItem ->
                val view = mBinding.mainRecyclerviewPastWords.layoutManager!!.findViewByPosition(
                    selectedItem.position
                )
                val option = ActivityOptions.makeSceneTransitionAnimation(
                    this@HomeActivity,
                    view!!,
                    "CONTAINER"
                )
                val intent = Intent(this@HomeActivity, WordDetailedActivity::class.java)
                val bundle = Bundle()
                bundle.putSerializable("WORD", selectedItem.data)
                intent.putExtras(bundle)
                startActivity(intent, option.toBundle())
            }
        })


        mViewModel.observeWordOfTheDayWork().observe(this, Observer {
            Log.d("HomeActivity", DailyWordWorker.TAG + " : " + Gson().toJson(it))
        })
        mViewModel.observeWordOfTheDayWork().observe(this, Observer {
            Log.d("HomeActivity", DailyWordWorker.TAG + " : " + Gson().toJson(it))
        })

        mViewModel.observeNewWordReminderWork().observe(this, Observer {
            Log.d("HomeActivity", NewWordReminderWorker.TAG + " : " + Gson().toJson(it))
        })
    }

    private fun edgeToEdgeSettingChanged() {
        WindowPreferencesManager.newInstance(this).getLiveData().observe(this, Observer<Boolean> {
            if (it) {
                restartActivitySmoothly()
            }
        })
    }

    override fun getViewModel(): HomeViewModel {
        return ViewModelProviders.of(this).get(HomeViewModel::class.java)
    }

    override fun getBindingVariable(): Int {
        return BR.mainViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_setting -> AppSettingActivity.openActivity(this@HomeActivity)

        }
        return super.onOptionsItemSelected(item)
    }

    private lateinit var bottomSheetDialog: BottomSheetDialog
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

    private fun shouldShowRatingDialog() {
        if (PrefManager.getInstance(this)
                .shouldShowRateNowDialog()
        ) {
            showStaticPageDialog(
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
            )
        }
    }

    var appUpdateHelper: AppUpdateHelper? = null;
    private fun initAppUpdate() {
        appUpdateHelper = AppUpdateHelper(this)
        appUpdateHelper?.checkForUpdate(object : AppUpdateHelper.AppUpdateAvailabilityListener {
            override fun onUpdateAvailable(appUpdateInfo: AppUpdateInfo) {
                appUpdateHelper?.startImmediateUpdate(appUpdateInfo) {
                    mViewModel.setMessage(SnackbarMessage.init(it))
                }
            }

            override fun onUpdateNotAvailable() {

            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        appUpdateHelper?.onActivityResult(requestCode, resultCode, data)
    }

}
