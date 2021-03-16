package com.pramod.dailyword.framework.ui.recap

import android.app.ActivityOptions
import android.os.Bundle
import android.transition.Transition
import android.util.Log
import androidx.activity.viewModels
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityRecapWordsBinding
import com.pramod.dailyword.framework.transition.TransitionCallback
import com.pramod.dailyword.framework.transition.removeCallbacks
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.exts.openWordDetailsPage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecapWordsActivity : BaseActivity<ActivityRecapWordsBinding, RecapWordsViewModel>() {

    override val layoutId: Int = R.layout.activity_recap_words
    override val viewModel: RecapWordsViewModel by viewModels()
    override val bindingVariable: Int = BR.recapWordsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        window.sharedElementsUseOverlay = true
        super.onCreate(savedInstanceState)
        setUpToolbar()
        initAdapter()
    }


    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.title = null
        }
        binding.toolbar.setNavigationIcon(R.drawable.ic_round_back_arrow)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initAdapter() {
        val adapter = RecapWordAdapter { pos, word ->

            setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
            window.sharedElementExitTransition.addListener(object : TransitionCallback() {
                override fun onTransitionEnd(transition: Transition) {
                    super.onTransitionEnd(transition)
                    Log.i("TAG", "onTransitionEnd: ")
                    removeCallbacks(this)
                }
            })

            val view = binding.recyclerViewRecapWords.layoutManager!!.findViewByPosition(pos)
            val option = ActivityOptions.makeSceneTransitionAnimation(
                this,
                view!!,
                word.date
            )

            openWordDetailsPage(word.date!!, option, windowAnimPrefManager.isEnabled())
        }
        binding.recyclerViewRecapWords.adapter = adapter
        mViewModel.words.observe(this) {
            adapter.submitList(it)
        }
    }

    override fun onBackPressed() {
        finish()
    }


    companion object {
        val TAG = RecapWordsActivity::class.java
    }
}