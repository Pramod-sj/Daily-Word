package com.pramod.dailyword.framework.ui.words

//import androidx.paging.ExperimentalPagingApi
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.databinding.ActivityWordListBinding
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.exts.openWordDetailsPage
import com.pramod.dailyword.framework.ui.common.word.WordsAdapter
import com.pramod.dailyword.framework.helper.AdsManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class WordListActivity : BaseActivity<ActivityWordListBinding, WordListViewModel>() {

    override val layoutId: Int = R.layout.activity_word_list
    override val viewModel: WordListViewModel by viewModels()
    override val bindingVariable: Int = BR.wordListViewModel


    companion object {
        @JvmStatic
        fun openActivity(context: Context) {
            val intent = Intent(context, WordListActivity::class.java)
            context.startActivity(intent)
        }

        @JvmStatic
        fun openActivity(context: Context, bundle: Bundle) {
            val intent = Intent(context, WordListActivity::class.java)
            context.startActivity(intent, bundle)
        }

        val TAG = WordListActivity::class.java.simpleName
    }


    @ExperimentalPagingApi
    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        initExitTransition()
        super.onCreate(savedInstanceState)
        setUpToolbar()
        initAdapter()
        setupSwipeToRefresh()
        findViewById<View>(android.R.id.content).postDelayed({
            showNativeAdDialogWithDelay()
        }, 150)

    }

    override fun onResume() {
        super.onResume()
        adapter?.setCanStartActivity(true)
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.title = null
        }
        binding.toolbar.setNavigationIcon(R.drawable.ic_round_back_arrow)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }


    private var adapter: WordsAdapter? = null

    @ExperimentalPagingApi
    @ExperimentalCoroutinesApi
    private fun initAdapter() {

        //val concatAdapter = ConcatAdapter()

        adapter = WordsAdapter { i: Int, word: Word ->
            Log.i(TAG, "initAdapter: ")
            val view = binding.recyclerviewWords.layoutManager!!.findViewByPosition(i)
            val option = ActivityOptions.makeSceneTransitionAnimation(
                this@WordListActivity,
                view!!,
                resources.getString(R.string.card_transition_name)
            )
            openWordDetailsPage(word.date!!, option)
        }.apply {
            withLoadStateFooter(NetworkStateAdapter {
                adapter?.retry()
            })
        }

        binding.recyclerviewWords.adapter = adapter

        //concatAdapter.addAdapter(adapter!!)

        lifecycleScope.launchWhenCreated {
            mViewModel.wordUIModelList.collectLatest {
                adapter?.submitData(it)
            }
        }

    }

    private fun setupSwipeToRefresh() {
        binding.swipeToRefresh.setOnRefreshListener {
            adapter?.refresh()
        }
        adapter?.addLoadStateListener {
            binding.swipeToRefresh.isRefreshing = it.refresh == LoadState.Loading
        }
    }
    /*private fun initTransition() {
        window.sharedElementsUseOverlay = true
        window.enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        window.exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        window.returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
    }*/

    private fun initExitTransition() {
        window.sharedElementsUseOverlay = false
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.allowEnterTransitionOverlap = true
        window.allowReturnTransitionOverlap = true
    }


    private fun showNativeAdDialogWithDelay() {
        Handler().postDelayed({
            AdsManager.incrementCountAndShowNativeAdDialog(this)
        }, 1000)
    }
}
