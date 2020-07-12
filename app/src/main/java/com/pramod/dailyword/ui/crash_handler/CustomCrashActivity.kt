package com.pramod.dailyword.ui.crash_handler

import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.WOTDApp
import com.pramod.dailyword.databinding.ActivityCustomCrashBinding
import com.pramod.dailyword.helper.WindowPrefManager
import com.pramod.dailyword.helper.openGmail
import com.pramod.dailyword.helper.restartApp
import com.pramod.dailyword.ui.BaseActivity
import com.pramod.dailyword.ui.home.HomeActivity

class CustomCrashActivity :
    BaseActivity<ActivityCustomCrashBinding, CustomCrashViewModel>() {

    override fun getLayoutId(): Int {
        return R.layout.activity_custom_crash
    }

    override fun getViewModel(): CustomCrashViewModel {
        val bundle = intent.extras
        val errorDetails = bundle!!.getString("errorDetails")!!
        return ViewModelProviders.of(
            this,
            CustomCrashViewModel.Factory(application, errorDetails)
        ).get(CustomCrashViewModel::class.java)
    }

    override fun getBindingVariable(): Int {
        return BR.customCrashViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initClearAppData()
        initCloseActivity()
        initRestartApp()
        initSendMail()
        arrangeViewsAccordingToEdgeToEdge()
    }

    private fun arrangeViewsAccordingToEdgeToEdge() {
        if (WindowPrefManager.newInstance(this).isEdgeToEdgeEnabled()) {
            ViewCompat.setOnApplyWindowInsetsListener(
                mBinding.root
            ) { v, insets ->
                mBinding.relativeParentLayotu.setPadding(
                    0, insets.systemWindowInsetTop, 0, insets.systemWindowInsetBottom
                )
                insets
            }
        }
    }

    private fun initCloseActivity() {
        mViewModel.closeActivityLiveData().observe(this, Observer {
            restartApp()
        })
    }

    private fun initClearAppData() {
        mViewModel.clearAppDataLiveData().observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                WOTDApp.clearAppData(this)
                restartApp()
            }
        })
    }

    private fun initSendMail() {
        mViewModel.sendMailLiveData().observe(this, Observer {
            it.getContentIfNotHandled()?.let { errorStack ->

                openGmail(
                    arrayOf(resources.getString(R.string.dev_email)),
                    "Error Logcat:" + resources.getString(R.string.app_name),
                    errorStack
                )
            }
        })
    }

    private fun initRestartApp() {
        mViewModel.restartAppLiveData().observe(this, Observer {
            it.getContentIfNotHandled().let {
                HomeActivity.openActivity(this)
                finishAffinity()
            }
        })
    }
}