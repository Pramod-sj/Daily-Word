package com.pramod.dailyword.framework.ui.bookmarks

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.databinding.ActivityFavoriteWordsBinding
import com.pramod.dailyword.framework.prefmanagers.WindowAnimPrefManager
import com.pramod.dailyword.framework.transition.isViewsPreDrawn
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.exts.openWordDetailsPage
import com.pramod.dailyword.framework.ui.common.exts.setUpToolbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@ExperimentalPagingApi
@AndroidEntryPoint
class FavoriteWordsActivity :
    BaseActivity<ActivityFavoriteWordsBinding, FavoriteWordsViewModel>(R.layout.activity_favorite_words) {

    override val viewModel: FavoriteWordsViewModel by viewModels()

    override val bindingVariable: Int = BR.favoriteWordsViewModel

    @Inject
    lateinit var windowAnimPrefManager: WindowAnimPrefManager

    private val adapter: BookmarkedWordsAdapter by lazy {
        BookmarkedWordsAdapter(itemClickCallback = { i: Int, word: Word ->
            val view = binding.recyclerviewWords.layoutManager!!.findViewByPosition(i)
            val option = ActivityOptions.makeSceneTransitionAnimation(
                this,
                view!!,
                word.date
            )
            openWordDetailsPage(
                word.date!!,
                option,
                windowAnimPrefManager.isEnabled()
            )
        }, deleteBookmarkCallback = {
            viewModel.removeBookmark(it)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpToolbar(binding.toolbar, null, true)
        bindAdapter()
    }

    override fun onResume() {
        super.onResume()
        adapter.setCanStartActivity(true)
    }

    private fun bindAdapter() {
        binding.recyclerviewWords.adapter = adapter

        adapter.addLoadStateListener {
            binding.recyclerviewWords.post {
                viewModel.showPlaceHolderLiveData.value = adapter.itemCount == 0
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.getFavWords().collectLatest { pagingData ->
                adapter.submitData(lifecycle, pagingData)
            }
        }

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