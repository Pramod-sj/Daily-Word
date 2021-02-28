package com.pramod.dailyword.framework.ui.bookmarks

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.google.gson.Gson
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.databinding.ActivityFavoriteWordsBinding
import com.pramod.dailyword.framework.prefmanagers.WindowAnimPrefManager
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.exts.openWordDetailsPage
import com.pramod.dailyword.framework.ui.common.word.WordsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalPagingApi
@AndroidEntryPoint
class FavoriteWordsActivity : BaseActivity<ActivityFavoriteWordsBinding, FavoriteWordsViewModel>() {

    override val layoutId: Int = R.layout.activity_favorite_words
    override val viewModel: FavoriteWordsViewModel by viewModels()
    override val bindingVariable: Int = BR.favoriteWordsViewModel

    @Inject
    lateinit var animPrefManager: WindowAnimPrefManager


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
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.title = null
        }
        binding.toolbar.setNavigationIcon(R.drawable.ic_round_back_arrow)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }


    private var adapter: WordsAdapter? = null

    private fun initAdapter() {
        adapter = WordsAdapter { i: Int, word: Word ->
            val view = binding.recyclerviewWords.layoutManager!!.findViewByPosition(i)
            val option = ActivityOptions.makeSceneTransitionAnimation(
                this,
                view!!,
                resources.getString(R.string.card_transition_name)
            )
            openWordDetailsPage(
                word.date!!,
                option,
                animPrefManager.isEnabled()
            )
        }
        binding.recyclerviewWords.adapter = adapter

        adapter?.addLoadStateListener {
            Log.i(TAG, "initAdapter: " + Gson().toJson(it))
            mViewModel.showPlaceHolderLiveData.value = adapter?.itemCount == 0
        }

        lifecycleScope.launch(Dispatchers.Main) {
            mViewModel.getFavWords().collectLatest { pagingData ->
                Log.i(TAG, "initAdapter: process")
                adapter?.submitData(lifecycle, pagingData)
                Log.i(TAG, "initAdapter: done" + adapter?.itemCount)
            }
        }


        /*   mViewModel.getFavWords().observe(this,
               {
                   mViewModel.showPlaceHolderLiveData.value = it.size == 0
                   adapter?.submitList(it)
               })*/
    }

}