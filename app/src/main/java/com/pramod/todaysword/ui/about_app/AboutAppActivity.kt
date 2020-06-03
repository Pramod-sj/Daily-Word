package com.pramod.todaysword.ui.about_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pramod.todaysword.BR
import com.pramod.todaysword.R
import com.pramod.todaysword.databinding.ActivityAboutAppBinding
import com.pramod.todaysword.helper.openGmail
import com.pramod.todaysword.helper.openGoogleReviewPage
import com.pramod.todaysword.helper.openWebsite
import com.pramod.todaysword.ui.BaseActivity
import com.pramod.todaysword.ui.about_app.donate.DonateActivity
import com.pramod.todaysword.ui.settings.AppSettingActivity

import kotlinx.android.synthetic.main.activity_about_app.*

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
        setAppLink()
        setDeveloperLink()
        setCreditLink()
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

        })
    }

    private fun setOthersLink() {
        mViewModel.showOpenSourceLibsLiveData().observe(this, Observer {
            it?.let {

            }
        })
        mViewModel.showPrivacyPolicyLiveData().observe(this, Observer {
            it?.let {

            }
        })
        mViewModel.showTermAndConditionLiveData().observe(this, Observer {
            it?.let {

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

    }


}
