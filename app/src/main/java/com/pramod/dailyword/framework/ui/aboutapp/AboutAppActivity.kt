package com.pramod.dailyword.framework.ui.aboutapp

import android.os.Bundle
import androidx.activity.viewModels
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.pramod.dailyword.BR
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityAboutAppBinding
import com.pramod.dailyword.framework.helper.openGmail
import com.pramod.dailyword.framework.helper.openGoogleReviewPage
import com.pramod.dailyword.framework.helper.openWebsite
import com.pramod.dailyword.framework.ui.changelogs.ChangelogDialogFragment
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.exts.setUpToolbar
import com.pramod.dailyword.framework.ui.common.exts.shareApp
import com.pramod.dailyword.framework.ui.common.exts.showBottomSheet
import com.pramod.dailyword.framework.ui.common.exts.showLib
import com.pramod.dailyword.framework.ui.dialog.WebViewDialogFragment
import com.pramod.dailyword.framework.ui.donate.DonateBottomDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutAppActivity :
    BaseActivity<ActivityAboutAppBinding, AboutAppViewModel>(R.layout.activity_about_app) {

    override val viewModel: AboutAppViewModel by viewModels()

    override val bindingVariable: Int = BR.aboutAppViewModel


    companion object {
        val TAG = AboutAppActivity::class.java.simpleName
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        initTransition()
        super.onCreate(savedInstanceState)
        setUpToolbar(binding.toolbar, null, true)
        setAppLink()
        setDeveloperLink()
        setCreditLink()
        setOthersLink()
    }

    private fun initTransition() {
        window.allowEnterTransitionOverlap = true
        window.allowReturnTransitionOverlap = true
        window.enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        window.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    private fun setAppLink() {
        viewModel.appLinkNavigate = object : AppLinkNavigate {
            override fun navigateToForkProject() {
                openWebsite(resources.getString(R.string.app_git_url))
            }

            override fun openGooglePlayReview() {
                openGoogleReviewPage()
            }

            override fun openDonatePage() {
                DonateBottomDialogFragment.show(supportFragmentManager)
            }

            override fun shareAppWithFriends() {
                shareApp()
            }

            override fun openChangelogActivity() {
                ChangelogDialogFragment.show(supportFragmentManager)
            }

        }
    }

    private fun setOthersLink() {

        viewModel.otherLinkNavigate = object : OtherLinkNavigate {
            override fun showTermsAndService() {
                //showWebViewDialog(EndPoints.TERM_AND_CONDITION)
                WebViewDialogFragment.show(
                    resources.getString(R.string.term_and_condition),
                    BuildConfig.TERM_AND_CONDITION,
                    supportFragmentManager
                )
            }

            override fun showPrivacyPolicy() {
                WebViewDialogFragment.show(
                    resources.getString(R.string.privacy_policy),
                    BuildConfig.PRIVACY_POLICY,
                    supportFragmentManager
                )
            }

            override fun showOpenSourceLibs() {
                showLib()
            }
        }

    }

    private fun setDeveloperLink() {

        viewModel.developerLinkNavigate = object : DeveloperLinkNavigate {
            override fun navigateToGithub() {
                openWebsite(resources.getString(R.string.dev_github_url))
            }

            override fun navigateToFacebook() {
                openWebsite(resources.getString(R.string.dev_facebook_url))
            }

            override fun navigateToGmail() {
                openGmail(
                    arrayOf(resources.getString(R.string.dev_email)),
                    "Daily Word Query",
                    "Hello Pramod,"
                )
            }

            override fun navigateToInstagram() {
                openWebsite(resources.getString(R.string.dev_instagram_url))
            }

        }

    }

    private fun setCreditLink() {
        viewModel.creditLinkNavigate = object : CreditLinkNavigate {
            override fun navigateToFreePikWebsite() {
                openWebsite(resources.getString(R.string.app_credit_freepik_url))
            }

            override fun navigateToMaterialDesignIcon() {
                openWebsite(resources.getString(R.string.app_credit_material_icon_url))
            }

            override fun navigateToMerriamWebster() {
                showBottomSheet(
                    "App Content Credit",
                    resources.getString(R.string.merriam_webster_credit_text),
                    positiveText = "Go to Merriam-Webster",
                    positiveClickCallback = {
                        openWebsite(resources.getString(R.string.app_merriam_webster_icon_url))
                    },
                    negativeText = "Close"
                )

            }

        }


    }


}
