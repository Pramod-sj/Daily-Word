package com.pramod.dailyword.framework.ui.splash_screen

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.paging.ExperimentalPagingApi
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivitySplashScreenBinding
import com.pramod.dailyword.framework.datasource.network.EndPoints
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.exts.*
import com.pramod.dailyword.framework.util.CommonUtils
import com.pramod.dailyword.framework.util.GradientUtils
import dagger.hilt.android.AndroidEntryPoint

@ExperimentalPagingApi
@AndroidEntryPoint
class SplashScreenActivity : BaseActivity<ActivitySplashScreenBinding, SplashScreenViewModel>() {

    override val layoutId: Int = R.layout.activity_splash_screen
    override val viewModel: SplashScreenViewModel by viewModels()
    override val bindingVariable: Int = BR.splashScreenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        forceEdgeToEdge(true)
        lightStatusBar(matchingBackgroundColor = true)
        super.onCreate(savedInstanceState)
        //addGradientToAppIcon()
        animateAppIcon()
        navigateToHomePage()
        setUpAcceptLinks()
    }

    private fun addGradientToAppIcon() {
        try {
            binding.splashAppIcon.setImageBitmap(
                GradientUtils.addGradient(
                    CommonUtils.drawableToBitmap(
                        getContextCompatDrawable(
                            R.drawable.ic_vocabulary
                        )!!
                    )!!,
                    getContextCompatColor(R.color.purple_200),
                    getContextCompatColor(R.color.green_600)
                )
            )
        } catch (e: Exception) {
            //if exception occur fallback to normal icon
            binding.splashAppIcon.setImageResource(R.drawable.ic_vocabulary)
            binding.splashAppIcon.imageTintList =
                ColorStateList.valueOf(getContextCompatColor(R.color.app_icon_tint))
        }
    }

    private fun navigateToHomePage() {
        mViewModel.navigateToHomePage().observe(this, {
            it.getContentIfNotHandled()?.let { startNavigate ->
                if (startNavigate) {
                    openHomePage(withFadeAnimation = true, finish = true)
                }
            }
        })
    }

    private fun animateAppIcon() {
        mViewModel.animateSplashIcon().observe(this, {
            if (it) {
                val initialXYDimen = resources.getDimension(R.dimen.splash_icon_big)
                val finalXYDimen = resources.getDimension(R.dimen.splash_icon_normal)
                CommonUtils.scaleXY(
                    binding.splashAppIcon,
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
        binding.acceptConditionTextView.showLinks(termsAndConditionLink, privacyPolicyLink)
    }


}
