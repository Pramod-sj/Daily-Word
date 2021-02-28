package com.pramod.dailyword.framework.ui.recap

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityRecapWordsBinding
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.exts.openWordDetailsPage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecapWordsActivity : BaseActivity<ActivityRecapWordsBinding, RecapWordsViewModel>() {

    override val layoutId: Int = R.layout.activity_recap_words
    override val viewModel: RecapWordsViewModel by viewModels()
    override val bindingVariable: Int = BR.recapWordsViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        initExitTransition()
        super.onCreate(savedInstanceState)
        initAdapter()
    }


    private fun initExitTransition() {
        window.sharedElementsUseOverlay = false
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.allowEnterTransitionOverlap = true
        window.allowReturnTransitionOverlap = true
    }

    private fun initAdapter() {
        val adapter = RecapWordAdapter { pos, word ->

            val view = binding.recyclerViewRecapWords.layoutManager!!.findViewByPosition(pos)
            val option = ActivityOptions.makeSceneTransitionAnimation(
                this,
                view!!,
                resources.getString(R.string.card_transition_name)
            )

            openWordDetailsPage(word.date!!, option, windowAnimPrefManager.isEnabled())
        }
        binding.recyclerViewRecapWords.adapter = adapter
        mViewModel.words.observe(this) {
            adapter.submitList(it)
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