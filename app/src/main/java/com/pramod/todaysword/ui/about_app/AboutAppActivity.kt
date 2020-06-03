package com.pramod.todaysword.ui.about_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.pramod.todaysword.BR
import com.pramod.todaysword.R
import com.pramod.todaysword.databinding.ActivityAboutAppBinding
import com.pramod.todaysword.helper.openGmail
import com.pramod.todaysword.helper.openWebsite
import com.pramod.todaysword.ui.BaseActivity
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
        setDeveloperLink()
        setCreditLink()
    }

    private fun setUpToolbar() {
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.let {
            it.title = null
        }
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

}
