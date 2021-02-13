package com.pramod.dailyword.ui.settings

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityAppSettingBinding
import com.pramod.dailyword.ui.BaseActivity

import com.pramod.dailyword.BR
import com.pramod.dailyword.helper.*
import com.pramod.dailyword.helper.WindowPrefManager
import com.pramod.dailyword.ui.about_app.AboutAppActivity
import dev.doubledot.doki.ui.DokiActivity

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
        edgeToEdgeSettingChanged()
        initThemeSelector()
        navigateToAbout()
        navigateToDokiActivity()
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
                    option.name,
                    "Apply",
                    "Cancel"
                ) { selectedThemeText ->
                    mViewModel.changeThemePref(ThemeManager.Options.valueOf(selectedThemeText))
                }
            }
        })
        mViewModel.themeManager.liveData().observe(this, {
            mViewModel.applyTheme(ThemeManager.Options.values()[it])
        })
    }

    private fun navigateToAbout() {
        mViewModel.navigateToAbout().observe(this) {
            it.getContentIfNotHandled()?.let { navigate ->
                if (navigate) {
                    AboutAppActivity.openActivity(
                        this
                    )
                }
            }
        }
    }

    private fun navigateToDokiActivity() {
        mViewModel.navigateToDokiActivity().observe(this) {
            it.getContentIfNotHandled()?.let { navigate ->
                if (navigate) {
                    DokiActivity.start(
                        this@AppSettingActivity
                    )
                }
            }
        }
    }

    private fun edgeToEdgeSettingChanged() {
        mViewModel.recreateActivity().observe(this, {
            it?.let {
                restartActivity()
            }
        })
    }

}
