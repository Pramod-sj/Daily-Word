package com.pramod.dailyword.ui.splash_screen

import android.os.Bundle
import android.transition.Fade
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.pramod.dailyword.R
import com.pramod.dailyword.BR
import com.pramod.dailyword.databinding.ActivitySplashScreenBinding
import com.pramod.dailyword.ui.BaseActivity
import com.pramod.dailyword.ui.home.HomeActivity
import com.pramod.dailyword.util.CommonUtils

class SplashScreenActivity : BaseActivity<ActivitySplashScreenBinding, SplashScreenViewModel>() {

    override fun getLayoutId(): Int = R.layout.activity_splash_screen
    override fun getViewModel(): SplashScreenViewModel =
        ViewModelProviders.of(this).get(SplashScreenViewModel::class.java)

    override fun getBindingVariable(): Int = BR.splashScreenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setUpExitTransition()
        super.onCreate(savedInstanceState)
        animateAppIcon()
        navigateToHomePage()
    }

    private fun setUpExitTransition() {
        val fade = Fade()
        fade.excludeTarget(android.R.id.navigationBarBackground, true)
        fade.excludeTarget(android.R.id.statusBarBackground, true)
        fade.duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()
        window.exitTransition = fade
    }

    private fun navigateToHomePage() {
        mViewModel.navigateToHomePage().observe(this, Observer {
            it.getContentIfNotHandled()?.let { startNavigate ->
                if (startNavigate) {
                    //val activityOptions = ActivityOptions.makeSceneTransitionAnimation(this)
                    HomeActivity.openActivity(this)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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

}
