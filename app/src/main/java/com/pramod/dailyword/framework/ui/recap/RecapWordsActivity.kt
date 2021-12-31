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
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.prefmanagers.WindowAnimPrefManager
import com.pramod.dailyword.framework.transition.TransitionCallback
import com.pramod.dailyword.framework.transition.removeCallbacks
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.exts.openWordDetailsPage
import com.pramod.dailyword.framework.ui.common.exts.setUpToolbar
import com.pramod.dailyword.framework.util.CalenderUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RecapWordsActivity :
    BaseActivity<ActivityRecapWordsBinding, RecapWordsViewModel>(R.layout.activity_recap_words) {

    override val viewModel: RecapWordsViewModel by viewModels()

    override val bindingVariable: Int = BR.recapWordsViewModel

    @Inject
    lateinit var windowAnimPrefManager: WindowAnimPrefManager

    @Inject
    lateinit var fbRemoteConfig: FBRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        window.sharedElementsUseOverlay = false
        super.onCreate(savedInstanceState)
        binding.adsEnabled = fbRemoteConfig.isAdsEnabled()
        binding.executePendingBindings()
        setUpToolbar(binding.toolbar, null, true)
        setWeeklyInfoText()
        initAdapter()
    }

    private fun setWeeklyInfoText() {
        viewModel.words.observe(this) { wordList ->
            binding.tvWeeklyRecapInfo.text = if (!wordList.isNullOrEmpty()) {

                val word1String = wordList.lastOrNull()?.let { word ->
                    CalenderUtil.getDayName(
                        word.dateTimeInMillis ?: 0L
                    ) + " (" + CalenderUtil.convertCalenderToString(
                        word.dateTimeInMillis ?: 0L,
                        CalenderUtil.DATE_WITH_YEAR_FORMAT_DISPLAY
                    ) + ")"
                }

                val word2String = wordList.firstOrNull()?.let { word ->
                    CalenderUtil.getDayName(
                        word.dateTimeInMillis ?: 0L
                    ) + " (" + CalenderUtil.convertCalenderToString(
                        word.dateTimeInMillis ?: 0L,
                        CalenderUtil.DATE_WITH_YEAR_FORMAT_DISPLAY
                    ) + ")"
                }

                String.format(
                    resources.getString(R.string.weekly_recap_note_with_date_placeholder),
                    word1String, word2String
                )
            } else
                String.format(resources.getString(R.string.weekly_recap_note))
        }
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

            val view = binding.recyclerViewRecapWords.layoutManager?.findViewByPosition(pos)
            val option = view?.let {
                ActivityOptions.makeSceneTransitionAnimation(
                    this,
                    view,
                    word.date
                )
            }
            word.date?.let { date ->
                openWordDetailsPage(
                    wordDate = date,
                    option = option,
                    shouldAnimate = windowAnimPrefManager.isEnabled(),
                    word = word
                )

            }
        }
        binding.recyclerViewRecapWords.adapter = adapter
        viewModel.words.observe(this) {
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