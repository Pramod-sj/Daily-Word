package com.pramod.dailyword.ui.about_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.pramod.dailyword.BR
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityAboutAppBinding
import com.pramod.dailyword.helper.*
import com.pramod.dailyword.ui.BaseActivity
import com.pramod.dailyword.ui.change_logs.ChangelogActivity
import com.pramod.dailyword.ui.donate.DonateActivity
import com.pramod.dailyword.util.shareApp

class AboutAppActivity : BaseActivity<ActivityAboutAppBinding, AboutAppViewModel>() {

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

    override fun getLayoutId(): Int = R.layout.activity_about_app


    override fun getViewModel(): AboutAppViewModel = ViewModelProviders.of(this)
        .get(AboutAppViewModel::class.java)

    override fun getBindingVariable(): Int {
        return BR.aboutAppViewModel
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
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.let {
            it.title = null
        }
        mBinding.toolbar.setNavigationIcon(R.drawable.ic_round_back_arrow)
        mBinding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setAppLink() {

        mBinding.setVariable(BR.appLinkNavigate, object : AppLinkNavigate {
            override fun navigateToForkProject() {
                openWebsite(resources.getString(R.string.app_git_url))
            }

            override fun openGooglePlayReview() {
                openGoogleReviewPage()
            }

            override fun openDonatePage() {
                DonateActivity.openActivity(this@AboutAppActivity)
            }

            override fun shareAppWithFriends() {
                shareApp()
            }

            override fun openChangelogActivity() {
                ChangelogActivity.openActivity(this@AboutAppActivity)
            }

        })


    }

    private fun setOthersLink() {

        mBinding.setVariable(BR.otherLinkNavigate, object : OtherLinkNavigate {
            override fun showTermsAndService() {
                showWebViewDialog(BuildConfig.TERM_AND_CONDITION)
            }

            override fun showPrivacyPolicy() {
                showWebViewDialog(BuildConfig.PRIVACY_POLICY)
            }

            override fun showOpenSourceLibs() {
                showLib()
            }

        })

    }

    private fun setDeveloperLink() {

        mBinding.setVariable(BR.developerLinkNavigate, object : DeveloperLinkNavigate {
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

        })

    }

    private fun setCreditLink() {
        mBinding.setVariable(BR.creditLinkNavigate, object : CreditLinkNavigate {
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

        })


    }


}
