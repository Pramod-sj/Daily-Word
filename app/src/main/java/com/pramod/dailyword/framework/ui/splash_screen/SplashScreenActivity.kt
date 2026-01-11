package com.pramod.dailyword.framework.ui.splash_screen

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.viewModels
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import com.pramod.dailyword.BR
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivitySplashScreenBinding
import com.pramod.dailyword.framework.haptics.HapticType
import com.pramod.dailyword.framework.helper.scheduleWeeklyAlarmAt12PM
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.exts.openHomePage
import com.pramod.dailyword.framework.ui.common.exts.openNotificationConsentPage
import com.pramod.dailyword.framework.ui.common.exts.showLinks
import com.pramod.dailyword.framework.ui.dialog.WebViewDialogFragment
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

    private var splashScreenViewProvider: SplashScreenViewProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        appPrefManager.incrementAppLaunchCount()
        //addGradientToAppIcon()
        navigateToHomePage()
        setUpAcceptLinks()
        scheduleWeeklyAlarmAt12PM()
        Timber.i("onCreate: " + intent.extras?.getString(DailyWordWidgetProvider.EXTRA_INTENT_TO_HOME_WORD_DATE))
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            // 1. Run your existing logic
            // splashScreenViewProvider = splashScreenViewProvider // Only keep this if you need to access it outside this scope
            viewModel.showSplashText()
            if (viewModel.isNewUser) {
                // Create your custom animation.
                val slideUp = ObjectAnimator.ofFloat(
                    splashScreenViewProvider.view,
                    View.TRANSLATION_Y,
                    0f,
                    -splashScreenViewProvider.view.height.toFloat()
                )
                slideUp.interpolator = AnticipateInterpolator()
                slideUp.duration = 200L
                // Call SplashScreenView.remove at the end of your custom animation.
                slideUp.doOnEnd { splashScreenViewProvider.remove() }
                // Run your animation.
                slideUp.start()
            } else {
                splashScreenViewProvider.remove()
            }
        }

        viewModel.splashScreenTextVisible().observe(this) {
            splashScreenViewProvider?.remove()
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

    private fun setUpAcceptLinks() {
        val termsAndConditionLink = Pair(
            resources.getString(R.string.term_and_condition_small),
            View.OnClickListener {
                hapticFeedbackManager.perform(HapticType.CLICK)
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
                hapticFeedbackManager.perform(HapticType.CLICK)
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
