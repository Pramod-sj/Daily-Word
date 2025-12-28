package com.pramod.dailyword.framework.ui.splash_screen

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.activity.viewModels
import com.pramod.dailyword.BR
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivitySplashScreenBinding
import com.pramod.dailyword.framework.helper.scheduleWeeklyAlarmAt12PM
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.exts.getContextCompatColor
import com.pramod.dailyword.framework.ui.common.exts.getContextCompatDrawable
import com.pramod.dailyword.framework.ui.common.exts.openHomePage
import com.pramod.dailyword.framework.ui.common.exts.openNotificationConsentPage
import com.pramod.dailyword.framework.ui.common.exts.showLinks
import com.pramod.dailyword.framework.ui.dialog.WebViewDialogFragment
import com.pramod.dailyword.framework.util.CommonUtils
import com.pramod.dailyword.framework.util.GradientUtils
import com.pramod.dailyword.framework.util.isImageCached
import com.pramod.dailyword.framework.util.preloadImage
import com.pramod.dailyword.framework.widget.DailyWordWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashScreenActivity :
    BaseActivity<ActivitySplashScreenBinding, SplashScreenViewModel>(R.layout.activity_splash_screen) {

    override val viewModel: SplashScreenViewModel by viewModels()

    override val bindingVariable: Int = BR.splashScreenViewModel

    @Inject
    lateinit var appPrefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appPrefManager.incrementAppLaunchCount()
        //addGradientToAppIcon()
        animateAppIcon()
        navigateToHomePage()
        setUpAcceptLinks()
        scheduleWeeklyAlarmAt12PM()
        Timber.i("onCreate: " + intent.extras?.getString(DailyWordWidgetProvider.EXTRA_INTENT_TO_HOME_WORD_DATE))
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
            binding.splashAppIcon.imageTintList
            ColorStateList.valueOf(getContextCompatColor(R.color.app_icon_tint))
        }
    }

    private fun navigateToHomePage() {
        viewModel.navigateToHomePage().observe(this) {
            it.getContentIfNotHandled()?.let { startNavigate ->
                if (startNavigate) {
                    isImageCached(BuildConfig.HOME_BACKGROUND_URL) { isCached ->
                        Timber.i("isImageCached: $isCached")
                        if (isCached) {
                            openHomePage(withFadeAnimation = true, finish = true)
                        } else {
                            binding.btnGetStarted.showProgress(true)
                            preloadImage(BuildConfig.HOME_BACKGROUND_URL) {
                                binding.btnGetStarted.showProgress(false)
                                Timber.i("preloadImage: $it")
                                openHomePage(withFadeAnimation = true, finish = true)
                            }
                        }
                    }

                }
            }
        }
        viewModel.navigateToNotificationConsent.observe(this) {
            it.getContentIfNotHandled()?.let { startNavigate ->
                if (startNavigate) {
                    isImageCached(BuildConfig.HOME_BACKGROUND_URL) { isCached ->
                        Timber.i("isImageCached: $isCached")
                        if (isCached) {
                            openNotificationConsentPage(withFadeAnimation = true, finish = true)
                        } else {
                            binding.btnGetStarted.showProgress(true)
                            preloadImage(BuildConfig.HOME_BACKGROUND_URL) {
                                binding.btnGetStarted.showProgress(false)
                                Timber.i("preloadImage: $it")
                                openNotificationConsentPage(withFadeAnimation = true, finish = true)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun animateAppIcon() {
        viewModel.animateSplashIcon().observe(this) {
            if (it) {

                CommonUtils.scaleXY(
                    binding.splashAppIcon,
                    -0.3f,
                    -0.3f,
                    1.0f,
                    1.0f,
                    1000L,
                    {},
                    { viewModel.showSplashText() }
                )
            } else {
                viewModel.showSplashText()
            }
        }
    }

    private fun setUpAcceptLinks() {
        val termsAndConditionLink = Pair(
            resources.getString(R.string.term_and_condition_small),
            View.OnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                WebViewDialogFragment.show(
                    resources.getString(R.string.term_and_condition_small),
                    BuildConfig.TERM_AND_CONDITION,
                    supportFragmentManager
                )
            }
        )

        val privacyPolicyLink = Pair(
            resources.getString(R.string.privacy_policy_small),
            View.OnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                WebViewDialogFragment.show(
                    resources.getString(R.string.privacy_policy_small),
                    BuildConfig.PRIVACY_POLICY,
                    supportFragmentManager
                )
            }
        )
        binding.acceptConditionTextView.showLinks(termsAndConditionLink, privacyPolicyLink)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Timber.i("onNewIntent: ")
    }

    companion object {
        val TAG = SplashScreenActivity::class.java.simpleName
    }

}
