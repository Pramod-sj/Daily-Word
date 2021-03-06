package com.pramod.dailyword.framework.ui.words

//import androidx.paging.ExperimentalPagingApi
import android.app.ActivityOptions
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.databinding.ActivityWordListBinding
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import com.pramod.dailyword.framework.prefmanagers.WindowAnimPrefManager
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.exts.openWordDetailsPage
import com.pramod.dailyword.framework.ui.common.exts.setUpToolbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class WordListActivity :
    BaseActivity<ActivityWordListBinding, WordListViewModel>(R.layout.activity_word_list) {

    override val viewModel: WordListViewModel by viewModels()

    override val bindingVariable: Int = BR.wordListViewModel

    @Inject
    lateinit var windowAnimPrefManager: WindowAnimPrefManager

    @Inject
    lateinit var prefManager: PrefManager

    private val adapter: WordsAdapter by lazy {
        WordsAdapter(
            itemClickCallback = { i: Int, word: Word ->

                setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())

                Log.i(TAG, "initAdapter: ")
                val view = binding.recyclerviewWords.layoutManager!!.findViewByPosition(i)
                val option = view?.let {
                    ActivityOptions.makeSceneTransitionAnimation(
                        this@WordListActivity,
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
            },
            bookmarkCallback = { i: Int, word: Word ->
                viewModel.toggleBookmark(word)
            },
            hideBadges = prefManager.getHideBadge()
        )
    }

    @ExperimentalPagingApi
    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpToolbar(binding.toolbar, null, true)
        initAdapter()
        setupSwipeToRefresh()
    }

    override fun onResume() {
        super.onResume()
        adapter.setCanStartActivity(true)
    }

    @ExperimentalPagingApi
    @ExperimentalCoroutinesApi
    private fun initAdapter() {

        binding.recyclerviewWords.adapter = adapter.withLoadStateFooter(
            NetworkStateAdapter {
                adapter.retry()
            })

        viewModel.wordUIModelList.observe(this@WordListActivity) {
            lifecycleScope.launch {
                adapter.submitData(it)
            }
        }

    }

    private fun setupSwipeToRefresh() {
        binding.swipeToRefresh.setOnRefreshListener {
            adapter.refresh()
        }
        adapter.addLoadStateListener {
            binding.swipeToRefresh.isRefreshing = it.refresh == LoadState.Loading
        }
    }

    override fun onBackPressed() {
        finish()
    }

    companion object {
        val TAG = WordListActivity::class.java.simpleName
    }

}
