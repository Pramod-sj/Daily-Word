package com.pramod.dailyword.framework.ui.splash_screen

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.activity.viewModels
import androidx.paging.ExperimentalPagingApi
import com.pramod.dailyword.BR
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivitySplashScreenBinding
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.exts.getContextCompatColor
import com.pramod.dailyword.framework.ui.common.exts.getContextCompatDrawable
import com.pramod.dailyword.framework.ui.common.exts.openHomePage
import com.pramod.dailyword.framework.ui.common.exts.showLinks
import com.pramod.dailyword.framework.ui.dialog.WebViewDialogFragment
import com.pramod.dailyword.framework.util.CommonUtils
import com.pramod.dailyword.framework.util.GradientUtils
import com.pramod.dailyword.framework.util.isImageCached
import com.pramod.dailyword.framework.util.preloadImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalPagingApi
@AndroidEntryPoint
class SplashScreenActivity :
    BaseActivity<ActivitySplashScreenBinding, SplashScreenViewModel>(R.layout.activity_splash_screen) {

    override val viewModel: SplashScreenViewModel by viewModels()

    override val bindingVariable: Int = BR.splashScreenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        forceEdgeToEdge(true)
        transparentNavBar = true
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
        viewModel.navigateToHomePage().observe(this, {
            it.getContentIfNotHandled()?.let { startNavigate ->
                if (startNavigate) {
                    isImageCached(BuildConfig.HOME_BACKGROUND_URL) { isCached ->
                        Log.i(TAG, "isImageCached: $isCached")
                        if (isCached) {
                            openHomePage(withFadeAnimation = true, finish = false)
                        } else {
                            binding.btnGetStarted.showProgress(true)
                            preloadImage(BuildConfig.HOME_BACKGROUND_URL) {
                                binding.btnGetStarted.showProgress(false)
                                Log.i(TAG, "preloadImage: $it")
                                openHomePage(withFadeAnimation = true, finish = false)
                            }
                        }
                    }

                }
            }
        })
    }

    private fun animateAppIcon() {
        viewModel.animateSplashIcon().observe(this, {
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
                    { viewModel.showSplashText() }
                )
            } else {
                viewModel.showSplashText()
            }
        })
    }

    private fun setUpAcceptLinks() {
        val termsAndConditionLink = Pair(
            resources.getString(R.string.term_and_condition),
            View.OnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                WebViewDialogFragment.show(
                    resources.getString(R.string.term_and_condition),
                    BuildConfig.TERM_AND_CONDITION,
                    supportFragmentManager
                )
            }
        )

        val privacyPolicyLink = Pair(
            resources.getString(R.string.privacy_policy),
            View.OnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                WebViewDialogFragment.show(
                    resources.getString(R.string.privacy_policy),
                    BuildConfig.PRIVACY_POLICY,
                    supportFragmentManager
                )
            }
        )
        binding.acceptConditionTextView.showLinks(termsAndConditionLink, privacyPolicyLink)
    }

    companion object {
        val TAG = SplashScreenActivity::class.java.simpleName
    }

}
