package com.pramod.todaysword.ui.settings

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.pramod.todaysword.R
import com.pramod.todaysword.databinding.ActivityAppSettingBinding
import com.pramod.todaysword.ui.BaseActivity

import com.pramod.todaysword.BR
import com.pramod.todaysword.helper.DailogHelper
import com.pramod.todaysword.helper.ThemeManager
import com.pramod.todaysword.ui.about_app.AboutAppActivity
import com.pramod.todaysword.ui.home.HomeActivity

class AppSettingActivity : BaseActivity<ActivityAppSettingBinding, AppSettingViewModel>() {


    companion object {
        @JvmStatic
        fun openActivity(context: Context) {
            val intent = Intent(context, AppSettingActivity::class.java)
            context.startActivity(intent)
        }
    }


    override fun getLayoutId(): Int = R.layout.activity_app_setting

    override fun getViewModel(): AppSettingViewModel =
        ViewModelProviders.of(this).get(AppSettingViewModel::class.java)

    override fun getBindingVariable(): Int = BR.appSettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpToolbar()
        initThemeSelector()
        navigateToAbout()
    }

    private fun setUpToolbar() {
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.let {
            it.title = null
        }
        mBinding.toolbar.setNavigationIcon(R.drawable.ic_round_back_arrow)
        mBinding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initThemeSelector() {
        mViewModel.getShowThemeSelector().observe(this, Observer {
            it.getContentIfNotHandled()?.let { option ->
                DailogHelper.showRadioDialog(
                    this@AppSettingActivity,
                    "Choose App Theme",
                    R.array.theme_options,
                    option.name
                ) { optionString ->
                    mViewModel.changeThemePref(
                        ThemeManager.Options.valueOf(optionString)
                    )
                }
            }
        })
    }

    private fun navigateToAbout() {
        mViewModel.navigateToAbout().observe(this, Observer {
            it.getContentIfNotHandled()?.let { navigate ->
                if (navigate) {
                    AboutAppActivity.openActivity(this)
                }
            }
        })
    }

}
