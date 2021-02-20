package com.pramod.dailyword.framework.ui.aboutapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityAboutAppBinding
import com.pramod.dailyword.framework.datasource.network.EndPoints
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.exts.openChangelogPage
import com.pramod.dailyword.framework.ui.common.exts.showBottomSheet
import com.pramod.dailyword.framework.ui.common.exts.showWebViewDialog
import com.pramod.dailyword.framework.helper.*
import com.pramod.dailyword.framework.ui.common.exts.shareApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutAppActivity : BaseActivity<ActivityAboutAppBinding, AboutAppViewModel>() {

    override val layoutId: Int = R.layout.activity_about_app
    override val viewModel: AboutAppViewModel by viewModels()
    override val bindingVariable: Int = BR.aboutAppViewModel


    companion object {
        @JvmStatic
        fun openActivity(context: Context) {
            val intent = Intent(context, AboutAppActivity::class.java)
            context.startActivity(intent)
        }

        @JvmStatic
        fun openActivity(context: Context, bundle: Bundle) {
            val intent = Intent(context, AboutAppActivity::class.java)
            context.startActivity(intent, bundle)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        initTransition()
        super.onCreate(savedInstanceState)
        setUpToolbar()
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

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.title = null
        }
        binding.toolbar.setNavigationIcon(R.drawable.ic_round_back_arrow)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setAppLink() {
        mViewModel.appLinkNavigate = object : AppLinkNavigate {
            override fun navigateToForkProject() {
                openWebsite(resources.getString(R.string.app_git_url))
            }

            override fun openGooglePlayReview() {
                openGoogleReviewPage()
            }

            override fun openDonatePage() {
                openDonatePage()
            }

            override fun shareAppWithFriends() {
                shareApp()
            }

            override fun openChangelogActivity() {
                openChangelogPage()
            }

        }
    }

    private fun setOthersLink() {

        mViewModel.otherLinkNavigate = object : OtherLinkNavigate {
            override fun showTermsAndService() {
                showWebViewDialog(EndPoints.TERM_AND_CONDITION)
            }

            override fun showPrivacyPolicy() {
                showWebViewDialog(EndPoints.PRIVACY_POLICY)
            }

            override fun showOpenSourceLibs() {
                showOpenSourceLibs()
            }
        }

    }

    private fun setDeveloperLink() {

        mViewModel.developerLinkNavigate = object : DeveloperLinkNavigate {
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
