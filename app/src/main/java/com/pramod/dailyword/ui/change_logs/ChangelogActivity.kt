package com.pramod.dailyword.ui.change_logs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.*
import androidx.databinding.DataBindingUtil.setContentView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityChangelogBinding
import com.pramod.dailyword.db.model.Changes
import com.pramod.dailyword.helper.WindowPrefManager
import com.pramod.dailyword.ui.BaseActivity
import com.pramod.dailyword.ui.BaseViewModel
import com.pramod.dailyword.ui.home.HomeActivity
import com.pramod.dailyword.util.CommonUtils
import kotlinx.android.synthetic.main.activity_changelog.*
import kotlinx.android.synthetic.main.activity_word_list.*
import kotlinx.android.synthetic.main.activity_word_list.appBar
import kotlinx.android.synthetic.main.activity_word_list.toolbar
import java.lang.reflect.Type

class ChangelogActivity : BaseActivity<ActivityChangelogBinding, BaseViewModel>() {


    override fun getLayoutId() = R.layout.activity_changelog

    override fun getViewModel() = BaseViewModel(application)

    override fun getBindingVariable() = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.setVariable(
            BR.showContinueButton, intent.getBooleanExtra(
                EXTRA_SHOW_CONTINUE_BUTTON, false
            )
        )
        arrangeViewsAccordingToEdgeToEdge()
        setUpToolbar()
        initChangelogAdapter()
        setUpCallbacks()
    }


    private fun setUpToolbar() {
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.let {
            it.title = null
        }
        mBinding.toolbar.setNavigationIcon(R.drawable.ic_round_close_24)
        mBinding.toolbar.setNavigationOnClickListener {
            finish()
            if(intent.getBooleanExtra(
                    EXTRA_SHOW_CONTINUE_BUTTON, false
                )) {
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
        mBinding.recyclerviewChangeLogs.adapter = changelogAdapter
    }

    private fun setUpCallbacks() {
        mBinding.setVariable(BR.changelogCallbacks, object : ChangelogCallbacks {
            override fun onContinueLearningWordsClick() {
                finish()
                overridePendingTransition(0, android.R.anim.fade_out)
            }

        })
    }

    private fun arrangeViewsAccordingToEdgeToEdge() {
        if (WindowPrefManager.newInstance(this).isEdgeToEdgeEnabled()) {
            ViewCompat.setOnApplyWindowInsetsListener(
                mBinding.root
            ) { v, insets ->
                appBar.setPadding(
                    0, insets.systemWindowInsetTop, 0, 0
                )

                val paddingTop = insets.systemWindowInsetTop + recyclerview_change_logs.paddingTop
                val paddingBottom = insets.systemWindowInsetBottom

                recyclerview_change_logs.setPadding(
                    0,
                    paddingTop,
                    0,
                    paddingBottom
                )


                val fabMarginBottom = mBinding.fabContinueLearningWords.marginBottom + paddingBottom
                val layoutParam: CoordinatorLayout.LayoutParams =
                    mBinding.fabContinueLearningWords.layoutParams as CoordinatorLayout.LayoutParams
                layoutParam.setMargins(
                    mBinding.fabContinueLearningWords.marginLeft,
                    mBinding.fabContinueLearningWords.marginTop,
                    mBinding.fabContinueLearningWords.marginRight,
                    fabMarginBottom
                )
                mBinding.fabContinueLearningWords.layoutParams = layoutParam


                insets
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

public interface ChangelogCallbacks {
    fun onContinueLearningWordsClick()
}