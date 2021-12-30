package com.pramod.dailyword.framework.ui.words

//import androidx.paging.ExperimentalPagingApi

import android.app.ActivityOptions
import android.app.SearchManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.EditText
import android.widget.SearchView
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@ExperimentalCoroutinesApi
@ExperimentalPagingApi
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

    private fun initAdapter() {

        binding.recyclerviewWords.adapter = adapter.withLoadStateFooter(
            NetworkStateAdapter {
                adapter.retry()
            })

        lifecycleScope.launch {
            viewModel.wordUIModelList.collectLatest {
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_word_list, menu)
        menu?.let { setUpSearchView(menu) }
        return super.onCreateOptionsMenu(menu)
    }

    private var queryTextChangedJob: Job? = null
    private fun setUpSearchView(menu: Menu) {
        val manager = getSystemService(SEARCH_SERVICE) as SearchManager

        val search: SearchView = menu.findItem(R.id.menu_search).actionView as SearchView
        search.maxWidth = Integer.MAX_VALUE
        search.queryHint = "Search by word"

        search.setSearchableInfo(manager.getSearchableInfo(componentName))

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                queryTextChangedJob?.cancel()
                queryTextChangedJob = lifecycleScope.launch {
                    delay(300)
                    viewModel.setSearchQuery(query ?: "")
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                queryTextChangedJob?.cancel()
                queryTextChangedJob = lifecycleScope.launch {
                    delay(300)
                    viewModel.setSearchQuery(newText ?: "")
                }
                return true
            }

        })
    }

    override fun onDestroy() {
        queryTextChangedJob?.cancel()
        queryTextChangedJob = null
        super.onDestroy()
    }

    companion object {
        val TAG = WordListActivity::class.java.simpleName
    }

}
