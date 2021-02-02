package com.pramod.dailyword.ui.bookmarked_words

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityFavoriteWordsBinding
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.helper.WindowPrefManager
import com.pramod.dailyword.ui.BaseActivity
import com.pramod.dailyword.ui.word_details.WordDetailedActivity
import com.pramod.dailyword.ui.words.WordListAdapter
import kotlinx.android.synthetic.main.activity_word_list.*

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
        findViewById<View>(android.R.id.content).postDelayed({
            initAdapter()
        }, 150)
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


    private var adapter: WordListAdapter? = null

    private fun initAdapter() {
        adapter = WordListAdapter { i: Int, wordOfTheDay: WordOfTheDay ->
            val view = recyclerview_words.layoutManager!!.findViewByPosition(i)
            val option = ActivityOptions.makeSceneTransitionAnimation(
                this,
                view!!,
                resources.getString(R.string.card_transition_name)
            )
            WordDetailedActivity.openActivity(this, wordOfTheDay, option)
        }
        mViewModel.getBookmarkedWords().observe(this, {
            mViewModel.showPlaceHolderLiveData.value = it.size == 0
            adapter?.submitList(it)
        })
        recyclerview_words.adapter = adapter
    }

}