package com.pramod.dailyword.ui.words

//import androidx.paging.ExperimentalPagingApi
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.google.gson.Gson
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityWordListBinding
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.helper.AdsManager
import com.pramod.dailyword.ui.BaseActivity
import com.pramod.dailyword.ui.word_details.WordDetailedActivity
import kotlinx.android.synthetic.main.activity_word_list.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest

class WordListActivity : BaseActivity<ActivityWordListBinding, WordListViewModel>() {

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

    override fun getLayoutId(): Int {
        return R.layout.activity_word_list
    }

    override fun getViewModel(): WordListViewModel {
        return ViewModelProviders.of(this)
            .get(WordListViewModel::class.java)
    }

    override fun getBindingVariable(): Int {
        return BR.wordListViewModel
    }

    @ExperimentalPagingApi
    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        lightStatusBar()
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
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.title = null
        }
        toolbar.setNavigationIcon(R.drawable.ic_round_back_arrow)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }


    private var adapter: WordsAdapter? = null

    @ExperimentalPagingApi
    @ExperimentalCoroutinesApi
    private fun initAdapter() {

        //val concatAdapter = ConcatAdapter()

        adapter = WordsAdapter { i: Int, wordOfTheDay: WordOfTheDay ->
            Log.i(TAG, "initAdapter: ")
            val view = recyclerview_words.layoutManager!!.findViewByPosition(i)
            val option = ActivityOptions.makeSceneTransitionAnimation(
                this@WordListActivity,
                view!!,
                resources.getString(R.string.card_transition_name)
            )
            WordDetailedActivity.openActivity(this, wordOfTheDay.date, option)
        }.apply {
            withLoadStateFooter(NetworkStateAdapter {
                adapter?.retry()
            })
        }

        recyclerview_words.adapter = adapter

        //concatAdapter.addAdapter(adapter!!)

        lifecycleScope.launchWhenCreated {
            mViewModel.wordUIModelList.collectLatest {
                adapter?.submitData(it)
            }
        }

    }

    private fun setupSwipeToRefresh() {
        mBinding.swipeToRefresh.setOnRefreshListener {
            adapter?.refresh()
        }
        adapter?.addLoadStateListener {
            mBinding.swipeToRefresh.isRefreshing = it.refresh == LoadState.Loading
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
