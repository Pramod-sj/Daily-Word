package com.pramod.dailyword.ui.words

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.google.gson.Gson
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityWordListBinding
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.helper.DividerItemDecoration
import com.pramod.dailyword.helper.WindowPreferencesManager
import com.pramod.dailyword.ui.BaseActivity
import com.pramod.dailyword.ui.word_details.WordDetailedActivity
import com.pramod.dailyword.util.CommonUtils

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

    override fun onCreate(savedInstanceState: Bundle?) {
        lightStatusBar()
        initExitTransition()
        super.onCreate(savedInstanceState)
        setUpToolbar()
        initAdapter()
        arrangeViewsAccordingToEdgeToEdge()
    }

    override fun onResume() {
        super.onResume()
        adapter?.setCanStartActivity(true)
    }

    private fun setUpToolbar() {
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.let {
            it.title = null
        }
        mBinding.toolbar.setNavigationIcon(R.drawable.ic_round_back_arrow)
        mBinding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private var adapter: WordListAdapter? = null
    private fun initAdapter() {
        adapter = WordListAdapter({ i: Int, wordOfTheDay: WordOfTheDay ->
            val view = mBinding.recyclerviewWords.layoutManager!!.findViewByPosition(i)
            val option = ActivityOptions.makeSceneTransitionAnimation(
                this@WordListActivity,
                view!!,
                "CONTAINER"
            )
            val intent = Intent(this@WordListActivity, WordDetailedActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("WORD", wordOfTheDay)
            intent.putExtras(bundle)
            startActivity(intent, option.toBundle())
        }, {
            mViewModel.retry()
        })
        mBinding.wordListAdapter = adapter
        mViewModel.words.observe(this, Observer {
            Log.i("DATA", it.size.toString())
            adapter?.submitList(it).apply {
                val layoutManager =
                    (mBinding.recyclerviewWords.layoutManager as LinearLayoutManager)
                val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                if (position != RecyclerView.NO_POSITION) {
                    mBinding.recyclerviewWords.scrollToPosition(position)
                }
            }
        })
        mViewModel.networkState.observe(this, Observer {
            adapter?.setNetworkState(it)
        })
        /*mBinding.recyclerviewWords.addItemDecoration(
            androidx.recyclerview.widget.DividerItemDecoration(
                this,
                (mBinding.recyclerviewWords.layoutManager as LinearLayoutManager).orientation
            )
        )*/
    }

    private fun initExitTransition() {
        window.sharedElementsUseOverlay = false
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
    }


    private fun arrangeViewsAccordingToEdgeToEdge() {
        if (WindowPreferencesManager.newInstance(this).isEdgeToEdgeEnabled()) {
            ViewCompat.setOnApplyWindowInsetsListener(
                mBinding.root
            ) { v, insets ->
                mBinding.appBar.setPadding(
                    0, insets.systemWindowInsetTop, 0, 0
                )

                val paddingTop = insets.systemWindowInsetTop + mBinding.recyclerviewWords.paddingTop
                val paddingBottom = insets.systemWindowInsetBottom

                mBinding.recyclerviewWords.setPadding(
                    0,
                    paddingTop,
                    0,
                    paddingBottom
                )

                mBinding.swipeToRefresh.setProgressViewOffset(true, paddingTop, 100 + paddingTop)

                insets
            }
        }

        val actionBarSize = CommonUtils.calculateActionBarHeight(this)
        mBinding.swipeToRefresh.setProgressViewOffset(true, actionBarSize, 100 + actionBarSize)

    }

}
