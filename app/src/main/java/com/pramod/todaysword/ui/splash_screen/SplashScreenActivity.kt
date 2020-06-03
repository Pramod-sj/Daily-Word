package com.pramod.todaysword.ui.splash_screen

import android.app.ActivityOptions
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.ArcMotion
import android.transition.Explode
import android.transition.Fade
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.pramod.todaysword.R
import com.pramod.todaysword.BR
import com.pramod.todaysword.databinding.ActivitySplashScreenBinding
import com.pramod.todaysword.ui.BaseActivity
import com.pramod.todaysword.ui.home.HomeActivity
import com.pramod.todaysword.util.CommonUtils

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
