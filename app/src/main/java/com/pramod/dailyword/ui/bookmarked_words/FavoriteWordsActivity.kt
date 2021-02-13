package com.pramod.dailyword.ui.bookmarked_words

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityFavoriteWordsBinding
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.ui.BaseActivity
import com.pramod.dailyword.ui.word_details.WordDetailedActivity
import com.pramod.dailyword.ui.words.WordsAdapter
import kotlinx.android.synthetic.main.activity_word_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FavoriteWordsActivity : BaseActivity<ActivityFavoriteWordsBinding, FavoriteWordsViewModel>() {
    companion object {
        @JvmStatic
        fun openActivity(context: Context) {
            val intent = Intent(context, FavoriteWordsActivity::class.java)
            context.startActivity(intent)
        }

        @JvmStatic
        fun openActivity(context: Context, bundle: Bundle) {
            val intent = Intent(context, FavoriteWordsActivity::class.java)
            context.startActivity(intent, bundle)
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_favorite_words

    override fun getViewModel(): FavoriteWordsViewModel =
        ViewModelProviders.of(this).get(FavoriteWordsViewModel::class.java)

    override fun getBindingVariable(): Int = BR.favoriteWordsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        initTransition()
        super.onCreate(savedInstanceState)
        setUpToolbar()
        initAdapter()
    }

    override fun onResume() {
        super.onResume()
        adapter?.setCanStartActivity(true)
    }


    private fun initTransition() {
        window.sharedElementsUseOverlay = false
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
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

    private fun initAdapter() {
        adapter = WordsAdapter { i: Int, wordOfTheDay: WordOfTheDay ->
            val view = recyclerview_words.layoutManager!!.findViewByPosition(i)
            val option = ActivityOptions.makeSceneTransitionAnimation(
                this,
                view!!,
                resources.getString(R.string.card_transition_name)
            )
            WordDetailedActivity.openActivity(this, wordOfTheDay.date!!, option)
        }
        mBinding.recyclerviewWords.adapter = adapter
        lifecycleScope.launch(Dispatchers.Main) {
            mViewModel.getFavWords().collectLatest { pagingData ->
                Log.i(TAG, "initAdapter: process")
                adapter?.submitData(lifecycle, pagingData)
                Log.i(TAG, "initAdapter: done" + adapter?.itemCount)
            }

            adapter?.loadStateFlow?.map { it.refresh }?.distinctUntilChanged()?.collect {
                if (it is LoadState.NotLoading) {
                    mViewModel.showPlaceHolderLiveData.value = adapter?.itemCount ?: 0 == 0
                }
            }
        }


        /*   mViewModel.getFavWords().observe(this,
               {
                   mViewModel.showPlaceHolderLiveData.value = it.size == 0
                   adapter?.submitList(it)
               })*/
    }

}