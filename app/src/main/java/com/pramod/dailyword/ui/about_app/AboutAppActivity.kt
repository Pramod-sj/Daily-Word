package com.pramod.dailyword.ui.about_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityAboutAppBinding
import com.pramod.dailyword.db.remote.EndPoints
import com.pramod.dailyword.helper.*
import com.pramod.dailyword.ui.BaseActivity
import com.pramod.dailyword.ui.about_app.donate.DonateActivity

class AboutAppActivity : BaseActivity<ActivityAboutAppBinding, AboutAppViewModel>() {

    companion object {
        @JvmStatic
        fun openActivity(context: Context) {
            val intent = Intent(context, AboutAppActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_about_app


    override fun getViewModel(): AboutAppViewModel = ViewModelProviders.of(this)
        .get(AboutAppViewModel::class.java)

    override fun getBindingVariable(): Int = BR.aboutAppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpToolbar()
        arrangeViewsAccordingToEdgeToEdge()
        setAppLink()
        setDeveloperLink()
        setCreditLink()
        setOthersLink()
    }

    private fun arrangeViewsAccordingToEdgeToEdge() {
        if (WindowPreferencesManager.newInstance(this).isEdgeToEdgeEnabled()) {
            ViewCompat.setOnApplyWindowInsetsListener(
                mBinding.root
            ) { v, insets ->
                mBinding.appBar.setPadding(
                    0, insets.systemWindowInsetTop, 0, 0
                )

                val paddingTop = insets.systemWindowInsetTop + mBinding.nestedScrollView.paddingTop
                val paddingBottom = insets.systemWindowInsetBottom

                mBinding.nestedScrollView.setPadding(
                    0,
                    paddingTop,
                    0,
                    paddingBottom
                )
                insets
            }
        }
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
        mViewModel.navigateToAppGithubLinkLiveData().observe(this, Observer {
            it?.let {
                openWebsite(resources.getString(R.string.app_git_url))
            }
        })
        mViewModel.navigateToGooglePlayReviewLiveData().observe(this, Observer {
            it?.let {
                openGoogleReviewPage()
            }
        })

        mViewModel.navigateToDonatePageLiveData().observe(this, Observer {
            it?.let {
                DonateActivity.openActivity(this)
            }
        })

        mViewModel.shareAppLiveData().observe(this, Observer {
            it?.let {
                shareApp()
            }
        })
    }

    private fun setOthersLink() {
        mViewModel.showOpenSourceLibsLiveData().observe(this, Observer {
            it?.let {
                showLib()
            }
        })
        mViewModel.showPrivacyPolicyLiveData().observe(this, Observer {
            it?.let {
                showWebViewDialog(EndPoints.PRIVACY_POLICY)
            }
        })
        mViewModel.showTermAndConditionLiveData().observe(this, Observer {
            it?.let {
                showWebViewDialog(EndPoints.TERM_AND_CONDITION)
            }
        })
    }

    private fun setDeveloperLink() {
        mViewModel.navigateToDevFacebookLiveData().observe(this, Observer {
            it?.let {
                openWebsite(resources.getString(R.string.dev_facebook_url))
            }
        })
        mViewModel.navigateToDevGithubLiveData().observe(this, Observer {
            it?.let {
                openWebsite(resources.getString(R.string.dev_github_url))
            }
        })
        mViewModel.navigateToDevInstagramLiveData().observe(this, Observer {
            it?.let {
                openWebsite(resources.getString(R.string.dev_instagram_url))
            }
        })
        mViewModel.navigateToDevGmailLiveData().observe(this, Observer {
            it?.let {
                openGmail(
                    arrayOf(resources.getString(R.string.dev_email)),
                    "What's your title?",
                    "Hello Pramod,"
                )
            }
        })

    }

    private fun setCreditLink() {
        mViewModel.navigateToFreepikLiveData().observe(this, Observer {
            it?.let {
                openWebsite(resources.getString(R.string.app_credit_freepik_url))
            }
        })


        mViewModel.navigateToMaterialIconLiveData().observe(this, Observer {
            it?.let {
                openWebsite(resources.getString(R.string.app_credit_material_icon_url))
            }
        })
        mViewModel.navigateToMerriamWebsterLiveData().observe(this, Observer {
            it?.let {
                openWebsite(resources.getString(R.string.app_merriam_webster_icon_url))
            }
        })

    }


}
