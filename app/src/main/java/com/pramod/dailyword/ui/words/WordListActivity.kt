package com.pramod.dailyword.ui.words

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.ConcatAdapter
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.google.gson.Gson
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityWordListBinding
import com.pramod.dailyword.db.model.Status
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.helper.AdsManager
import com.pramod.dailyword.helper.WindowPrefManager
import com.pramod.dailyword.ui.BaseActivity
import com.pramod.dailyword.ui.word_details.WordDetailedActivity
import com.pramod.dailyword.util.CommonUtils
import kotlinx.android.synthetic.main.activity_word_list.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

class WordListActivity : BaseActivity<ActivityWordListBinding, WordListViewModel>() {

    companion object {
        @JvmStatic
        fun openActivity(context: Context) {
            val intent = Intent(context, WordListActivity::class.java)
            context.startActivity(intent)
        }
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

    @ExperimentalCoroutinesApi
    @ExperimentalPagingApi
    override fun onCreate(savedInstanceState: Bundle?) {
        lightStatusBar()
        initExitTransition()
        super.onCreate(savedInstanceState)
        setUpToolbar()
        initAdapter()
        arrangeViewsAccordingToEdgeToEdge()
        showNativeAdDialogWithDelay()
    }

    override fun onResume() {
        super.onResume()
        adapter.setCanStartActivity(true)
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.title = null
        }
        toolbar.setNavigationIcon(R.drawable.ic_round_back_arrow)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }


    private lateinit var adapter: WordListAdapter
    private var networkStateAdapter: NetworkStateAdapter? = null

    @ExperimentalCoroutinesApi
    @ExperimentalPagingApi
    private fun initAdapter() {
        val concatAdapter = ConcatAdapter()

        adapter = WordListAdapter { i: Int, wordOfTheDay: WordOfTheDay ->
            val view = recyclerview_words.layoutManager!!.findViewByPosition(i)
            val option = ActivityOptions.makeSceneTransitionAnimation(
                this@WordListActivity,
                view!!,
                "CONTAINER"
            )
            WordDetailedActivity.openActivity(this, wordOfTheDay, option)
        }
        concatAdapter.addAdapter(adapter)
        mViewModel.networkState.observe(this, Observer {
            Log.i("NETWORK STATE", Gson().toJson(it))
            networkStateAdapter?.let { adapter ->
                concatAdapter.removeAdapter(adapter)
            }
            if (it.status != Status.SUCCESS) {
                networkStateAdapter = NetworkStateAdapter(it) {
                    mViewModel.retry()
                }.also { adapter ->
                    concatAdapter.addAdapter(adapter)
                }
            }
        })
        mViewModel.words.observe(this, Observer {
            adapter.submitList(it)
        })
        recyclerview_words.adapter = concatAdapter
    }

    private fun initExitTransition() {
        window.sharedElementsUseOverlay = false
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
    }

    private fun arrangeViewsAccordingToEdgeToEdge() {
        if (WindowPrefManager.newInstance(this).isEdgeToEdgeEnabled()) {
            ViewCompat.setOnApplyWindowInsetsListener(
                mBinding.root
            ) { v, insets ->
                appBar.setPadding(
                    0, insets.systemWindowInsetTop, 0, 0
                )

                val paddingTop = insets.systemWindowInsetTop + recyclerview_words.paddingTop
                val paddingBottom = insets.systemWindowInsetBottom

                recyclerview_words.setPadding(
                    0,
                    paddingTop,
                    0,
                    paddingBottom
                )

                swipeToRefresh.setProgressViewOffset(true, paddingTop, 100 + paddingTop)

                insets
            }
        }

        val actionBarSize = CommonUtils.calculateActionBarHeight(this)
        swipeToRefresh.setProgressViewOffset(true, actionBarSize, 100 + actionBarSize)

    }

    private fun showNativeAdDialogWithDelay() {
        Handler().postDelayed({
            AdsManager.incrementCountAndShowNativeAdDialog(this)
        }, 1000)
    }
}
