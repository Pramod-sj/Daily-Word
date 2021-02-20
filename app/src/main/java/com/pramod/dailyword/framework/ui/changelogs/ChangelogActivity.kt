package com.pramod.dailyword.framework.ui.changelogs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityChangelogBinding
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.util.CommonUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangelogActivity : BaseActivity<ActivityChangelogBinding, ChangelogViewModel>() {

    override val layoutId: Int = R.layout.activity_changelog
    override val viewModel: ChangelogViewModel by viewModels()
    override val bindingVariable: Int = BR.changelogViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.setVariable(
            BR.showContinueButton, intent.getBooleanExtra(
                EXTRA_SHOW_CONTINUE_BUTTON, false
            )
        )
        setUpToolbar()
        initChangelogAdapter()
        setUpCallbacks()
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.title = null
        }
        binding.toolbar.setNavigationIcon(R.drawable.ic_round_close_24)
        binding.toolbar.setNavigationOnClickListener {
            finish()
            if (intent.getBooleanExtra(
                    EXTRA_SHOW_CONTINUE_BUTTON, false
                )
            ) {
                overridePendingTransition(
                    0,
                    android.R.anim.fade_out
                )
            }
        }
    }


    private fun initChangelogAdapter() {
        val type = TypeToken.getParameterized(List::class.java, Changes::class.java).type
        val changelogList =
            Gson().fromJson<List<Changes>>(
                CommonUtils.loadJsonFromAsset(this, "change_logs.json"),
                type
            )
        val changelogAdapter = ChangelogAdapter(changelogList)
        binding.recyclerviewChangeLogs.adapter = changelogAdapter
    }

    private fun setUpCallbacks() {
        mViewModel.navigator = object : ChangelogNavigator {
            override fun onContinueLearningWordsClick() {
                finish()
                overridePendingTransition(0, android.R.anim.fade_out)
            }
        }
    }

    companion object {
        const val EXTRA_SHOW_CONTINUE_BUTTON = "showContinueButton"

        @JvmStatic
        fun openActivity(context: Context, showContinueButton: Boolean = false) {
            val intent = Intent(context, ChangelogActivity::class.java)
            if (context is Activity) {
                context.overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            }
            intent.putExtra(EXTRA_SHOW_CONTINUE_BUTTON, showContinueButton)
            context.startActivity(intent)
        }
    }

}
