package com.pramod.todaysword.ui.crash_handler

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.pramod.todaysword.BR
import com.pramod.todaysword.BuildConfig
import com.pramod.todaysword.R
import com.pramod.todaysword.WOTDApp
import com.pramod.todaysword.databinding.ActivityCustomCrashBinding
import com.pramod.todaysword.helper.openGmail
import com.pramod.todaysword.helper.restartApp
import com.pramod.todaysword.ui.BaseActivity
import com.pramod.todaysword.ui.home.HomeActivity

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