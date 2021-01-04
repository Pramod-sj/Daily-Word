package com.pramod.dailyword.ui.recapwords

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityRecapWordsBinding
import com.pramod.dailyword.ui.BaseActivity
import com.pramod.dailyword.ui.word_details.WordDetailedActivity
import kotlinx.android.synthetic.main.activity_word_list.*

class RecapWordsActivity : BaseActivity<ActivityRecapWordsBinding, RecapWordsViewModel>() {

    override fun getLayoutId(): Int = R.layout.activity_recap_words

    override fun getViewModel(): RecapWordsViewModel =
        ViewModelProviders.of(this).get(RecapWordsViewModel::class.java)

    override fun getBindingVariable(): Int = BR.recapWordsViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        initExitTransition()
        super.onCreate(savedInstanceState)
        setUpToolbar()
        initAdapter()
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.title = null
        }
        toolbar.setNavigationIcon(R.drawable.ic_round_back_arrow)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initExitTransition() {
        window.sharedElementsUseOverlay = false
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.allowEnterTransitionOverlap = true
        window.allowReturnTransitionOverlap = true
    }

    private fun initAdapter() {
        val adapter = RecapWordAdapter { pos, word ->

            val view = mBinding.recyclerViewRecapWords.layoutManager!!.findViewByPosition(pos)
            val option = ActivityOptions.makeSceneTransitionAnimation(
                this,
                view!!,
                resources.getString(R.string.card_transition_name)
            )

            WordDetailedActivity.openActivity(this, word.date!!, option)
        }
        mBinding.recyclerViewRecapWords.adapter = adapter
        mViewModel.words.observe(this) {
            adapter.submitList(it)
        }
        mViewModel.words.observe(this) {
            it?.let { data ->

                for (d in data) {
                    Log.i(TAG, "loadLocalData: ${d.date} ${d.dateTimeInMillis}")
                }
            }
        }
    }

    companion object {
        @JvmStatic
        public fun openActivity(context: Context) {
            val intent = Intent(context, RecapWordsActivity::class.java)
            context.startActivity(intent)
        }
    }
}