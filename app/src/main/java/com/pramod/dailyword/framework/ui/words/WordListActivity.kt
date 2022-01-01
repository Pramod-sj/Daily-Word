package com.pramod.dailyword.framework.ui.words

//import androidx.paging.ExperimentalPagingApi

import android.app.ActivityOptions
import android.app.SearchManager
import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.google.firebase.analytics.FirebaseAnalytics
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.databinding.ActivityWordListBinding
import com.pramod.dailyword.framework.helper.openWebsite
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import com.pramod.dailyword.framework.prefmanagers.WindowAnimPrefManager
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.exts.openWordDetailsPage
import com.pramod.dailyword.framework.ui.common.exts.setUpToolbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class WordListActivity :
    BaseActivity<ActivityWordListBinding, WordListViewModel>(R.layout.activity_word_list) {

    override val viewModel: WordListViewModel by viewModels()

    override val bindingVariable: Int = BR.wordListViewModel

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    @Inject
    lateinit var windowAnimPrefManager: WindowAnimPrefManager

    @Inject
    lateinit var prefManager: PrefManager

    private var contentInsetStartWithNavigation = 0

    private val adapter: WordsAdapter by lazy {
        WordsAdapter(
            itemClickCallback = { i: Int, word: Word ->

                setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())

                Timber.i( "initAdapter: ")
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
        contentInsetStartWithNavigation = binding.toolbar.contentInsetStartWithNavigation
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

    private val loadStateListener = { states: CombinedLoadStates ->
        if (searchView?.query.toString().isNotBlank()) {
            if (adapter.snapshot().size == 0) {
                binding.inclPlaceholder.placeHolderTitle =
                    String.format(
                        resources.getString(R.string.no_search_result_placeholder),
                        searchView?.query.toString()
                    )
                binding.inclPlaceholder.placeHolderText =
                    resources.getString(R.string.no_search_result_search_on_web)
                binding.inclPlaceholder.show = true
                binding.inclPlaceholder.root.setOnClickListener {
                    firebaseAnalytics.logEvent(
                        "not_found_word_search", bundleOf(
                            "word" to searchView?.query.toString()
                        )
                    )
                    openWebsite(resources.getString(R.string.google_search_url) + searchView?.query.toString())
                }
                binding.inclPlaceholder.executePendingBindings()
            } else {
                binding.inclPlaceholder.show = false
                binding.inclPlaceholder.root.setOnClickListener {}
                binding.inclPlaceholder.executePendingBindings()
            }
        } else {
            binding.inclPlaceholder.show = false
            binding.inclPlaceholder.executePendingBindings()
        }
    }

    private fun setupSwipeToRefresh() {
        binding.swipeToRefresh.setOnRefreshListener {
            adapter.refresh()
        }
        adapter.addLoadStateListener(loadStateListener)
        lifecycleScope.launch {
            adapter.loadStateFlow.map { it.refresh }.collectLatest {
                binding.swipeToRefresh.isRefreshing = it == LoadState.Loading
            }
        }
    }

    override fun onBackPressed() {
        if (searchView?.isIconified == false) searchView?.isIconified = true else finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_word_list, menu)
        menu?.let { setUpSearchView(menu) }
        return super.onCreateOptionsMenu(menu)
    }

    private var queryTextChangedJob: Job? = null
    private var searchView: SearchView? = null
    private fun setUpSearchView(menu: Menu) {
        val manager = getSystemService(SEARCH_SERVICE) as SearchManager
        val searchMenuItem = menu.findItem(R.id.menu_search)
        searchView = searchMenuItem?.actionView as? SearchView
        searchView?.let { search ->
            search.maxWidth = Integer.MAX_VALUE
            search.queryHint = resources.getString(R.string.search_hint)
            search.setOnCloseListener {
                binding.toolbar.contentInsetStartWithNavigation = contentInsetStartWithNavigation
                false
            }
            search.setOnSearchClickListener {
                binding.toolbar.contentInsetStartWithNavigation = 0
            }
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
    }

    override fun onDestroy() {
        queryTextChangedJob?.cancel()
        queryTextChangedJob = null
        adapter.removeLoadStateListener(loadStateListener)
        super.onDestroy()
    }

    companion object {
        val TAG = WordListActivity::class.java.simpleName
    }

}
