package com.pramod.dailyword.framework.ui.splash_screen

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.viewModels
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.coroutineScope
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        appPrefManager.incrementAppLaunchCount()
        keepSplashUntilSpecifiedDuration(splashScreen)
        navigateToHomePage()
        setUpAcceptLinks()
        scheduleWeeklyAlarmAt12PM()
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            if (viewModel.isNewUser) {
                val slideUp = ObjectAnimator.ofFloat(
                    splashScreenViewProvider.view,
                    View.TRANSLATION_Y,
                    0f,
                    -splashScreenViewProvider.view.height.toFloat()
                )
                slideUp.interpolator = AnticipateInterpolator()
                slideUp.duration = 200L
                slideUp.doOnEnd { splashScreenViewProvider.remove() }
                slideUp.start()
            } else {
                splashScreenViewProvider.remove()
            }
        }
        lifecycle.coroutineScope.launch {
            // This isn't a good fix, but since it was affecting a lot of users, so adding a patch
            delay(resources.getInteger(R.integer.splash_anim_duration).toLong())
            viewModel.showSplashText()
        }
    }

    private fun keepSplashUntilSpecifiedDuration(splashScreen: SplashScreen) {
        val animationDuration = resources.getInteger(R.integer.splash_anim_duration)
        val startTime = System.currentTimeMillis()
        splashScreen.setKeepOnScreenCondition {
            val elapsedTime = System.currentTimeMillis() - startTime
            elapsedTime < animationDuration
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
