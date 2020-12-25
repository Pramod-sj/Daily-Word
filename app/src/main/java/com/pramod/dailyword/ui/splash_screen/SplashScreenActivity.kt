package com.pramod.dailyword.ui.splash_screen

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pramod.dailyword.R
import com.pramod.dailyword.BR
import com.pramod.dailyword.databinding.ActivitySplashScreenBinding
import com.pramod.dailyword.db.remote.EndPoints
import com.pramod.dailyword.firebase.FBTopicSubscriber
import com.pramod.dailyword.helper.NotificationPrefManager
import com.pramod.dailyword.helper.showWebViewDialog
import com.pramod.dailyword.ui.BaseActivity
import com.pramod.dailyword.ui.home.HomeActivity
import com.pramod.dailyword.util.CommonUtils
import com.pramod.dailyword.util.showLinks
import kotlinx.android.synthetic.main.activity_splash_screen.*
import com.pramod.dailyword.helper.ThemeManager
import com.pramod.dailyword.helper.PrefManager


class SplashScreenActivity : BaseActivity<ActivitySplashScreenBinding, SplashScreenViewModel>() {

    override fun getLayoutId(): Int = R.layout.activity_splash_screen
    override fun getViewModel(): SplashScreenViewModel =
        ViewModelProviders.of(this).get(SplashScreenViewModel::class.java)

    override fun getBindingVariable(): Int = BR.splashScreenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        forceEdgeToEdge(true)
        lightStatusBar(
            if (PrefManager.getInstance(this).isNewUser()) false
            else !ThemeManager.isNightModeActive(this)
        )
        super.onCreate(savedInstanceState)
        registerTopics()
        animateAppIcon()
        navigateToHomePage()
        setUpAcceptLinks()
    }

    private fun navigateToHomePage() {
        mViewModel.navigateToHomePage().observe(this, {
            it.getContentIfNotHandled()?.let { startNavigate ->
                if (startNavigate) {
                    //val activityOptions = ActivityOptions.makeSceneTransitionAnimation(this)
                    HomeActivity.openActivityWithFade(this)
                    finish()
                    //ActivityCompat.finishAfterTransition(this)
                }
            }
        })
    }

    private fun animateAppIcon() {
        mViewModel.animateSplashIcon().observe(this, Observer {
            if (it) {
                val initialXYDimen = resources.getDimension(R.dimen.splash_icon_big)
                val finalXYDimen = resources.getDimension(R.dimen.splash_icon_normal)
                CommonUtils.scaleXY(
                    mBinding.splashAppIcon,
                    initialXYDimen,
                    initialXYDimen,
                    finalXYDimen,
                    finalXYDimen,
                    1000L,
                    {},
                    { mViewModel.showSplashText() }
                )
            } else {
                mViewModel.showSplashText()
            }
        })
    }

    private fun setUpAcceptLinks() {
        val termsAndConditionLink = Pair(
            resources.getString(R.string.term_and_condition),
            View.OnClickListener {
                showWebViewDialog(EndPoints.TERM_AND_CONDITION)
            }
        )

        val privacyPolicyLink = Pair(
            resources.getString(R.string.privacy_policy),
            View.OnClickListener {
                showWebViewDialog(EndPoints.PRIVACY_POLICY)
            }
        )
        accept_condition_textView.showLinks(termsAndConditionLink, privacyPolicyLink)
    }

    private fun registerTopics() {
        FBTopicSubscriber.subscribeToDailyWordNotification()
        FBTopicSubscriber.subscribeToCountry(this)
    }

    override fun arrangeViewsForEdgeToEdge(view: View, insets: WindowInsetsCompat) {
        val paddingBottom = insets.systemWindowInsetBottom

        mBinding.splashScreenBottomLayout.setPadding(
            0,
            0,
            0,
            paddingBottom
        )
    }
}
