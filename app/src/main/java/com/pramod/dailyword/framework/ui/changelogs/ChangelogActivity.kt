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
import com.pramod.dailyword.framework.ui.common.exts.setUpToolbar
import com.pramod.dailyword.framework.util.CommonUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangelogActivity :
    BaseActivity<ActivityChangelogBinding, ChangelogViewModel>(R.layout.activity_changelog) {

    override val viewModel: ChangelogViewModel by viewModels()

    override val bindingVariable: Int = BR.changelogViewModel

    private val adapter: ChangelogAdapter by lazy {
        val type = TypeToken.getParameterized(List::class.java, Changes::class.java).type
        val changelogList =
            Gson().fromJson<List<Changes>>(
                CommonUtils.loadJsonFromAsset(this, "change_logs.json"),
                type
            )
        ChangelogAdapter(changelogList)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.setVariable(
            BR.showContinueButton, intent.getBooleanExtra(
                EXTRA_SHOW_CONTINUE_BUTTON, false
            )
        )
        setUpToolbar(binding.toolbar, null, true, navIconClickListener = {
            finish()
            if (intent.getBooleanExtra(EXTRA_SHOW_CONTINUE_BUTTON, false)) {
                overridePendingTransition(0, android.R.anim.fade_out)
            }
        })
        bindingAdapter()
        setUpCallbacks()
    }

    private fun bindingAdapter() {
        binding.recyclerviewChangeLogs.adapter = adapter
    }

    private fun setUpCallbacks() {
        viewModel.navigator = object : ChangelogNavigator {
            override fun onContinueLearningWordsClick() {
                finish()
                overridePendingTransition(0, android.R.anim.fade_out)
            }
        }
    }

    companion object {
        const val EXTRA_SHOW_CONTINUE_BUTTON = "showContinueButton"
    }

}
