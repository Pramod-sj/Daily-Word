package com.pramod.dailyword.framework.ui.bookmarks

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.databinding.ActivityFavoriteWordsBinding
import com.pramod.dailyword.framework.transition.isViewsPreDrawn
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.exts.openWordDetailsPage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@ExperimentalPagingApi
@AndroidEntryPoint
class FavoriteWordsActivity : BaseActivity<ActivityFavoriteWordsBinding, FavoriteWordsViewModel>() {

    override val layoutId: Int = R.layout.activity_favorite_words
    override val viewModel: FavoriteWordsViewModel by viewModels()
    override val bindingVariable: Int = BR.favoriteWordsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        //window.sharedElementsUseOverlay = true
        super.onCreate(savedInstanceState)
        setUpToolbar()
        //addSwipeListenerOnRecyclerViewItems()
        initAdapter()
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


    private var adapter: BookmarkedWordsAdapter? = null

    private fun initAdapter() {
        adapter = BookmarkedWordsAdapter(itemClickCallback = { i: Int, word: Word ->
            val view = binding.recyclerviewWords.layoutManager!!.findViewByPosition(i)
            val option = ActivityOptions.makeSceneTransitionAnimation(
                this,
                view!!,
                resources.getString(R.string.card_transition_name)
            )
            openWordDetailsPage(
                word.date!!,
                option,
                windowAnimPrefManager.isEnabled()
            )
        }, deleteBookmarkCallback = {
            viewModel.removeBookmark(it)
        })
        binding.recyclerviewWords.adapter = adapter

        adapter?.addLoadStateListener {
            Log.i(TAG, "initAdapter: " + it.append)
            binding.recyclerviewWords.post {
                mViewModel.showPlaceHolderLiveData.value = adapter?.itemCount == 0
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            mViewModel.getFavWords().collectLatest { pagingData ->
                Log.i(TAG, "initAdapter: process")
                adapter?.submitData(lifecycle, pagingData)
                Log.i(TAG, "initAdapter: done" + adapter?.itemCount)
            }
        }

    }

    private fun addSwipeListenerOnRecyclerViewItems() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return makeMovementFlags(
                    ItemTouchHelper.ACTION_STATE_IDLE,
                    ItemTouchHelper.START or ItemTouchHelper.END or ItemTouchHelper.UP or ItemTouchHelper.DOWN
                )
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                adapter?.let {
                    /*viewModel.processStateEvent(
                        GalleryStateEvent.DeletePostStateEvent(
                            it.getPost(viewHolder.adapterPosition)
                        )
                    )*/
                }

            }


        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerviewWords)
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        /*val wasChanged = WordDetailedActivity.wasBookmarkStatusChanged(resultCode, data?.extras)
        if (wasChanged) {
            binding.recyclerviewWords.scrollToPosition(0)
        }*/

        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        supportPostponeEnterTransition()
        isViewsPreDrawn(binding.recyclerviewWords) {
            supportStartPostponedEnterTransition()
        }

    }
}