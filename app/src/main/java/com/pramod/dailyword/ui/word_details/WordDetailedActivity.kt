package com.pramod.dailyword.ui.word_details

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Color
import com.pramod.dailyword.BR
import android.os.Bundle
import android.os.Handler
import android.transition.ArcMotion
import android.transition.Fade
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.*
import androidx.core.widget.NestedScrollView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.google.gson.Gson
import com.pramod.dailyword.databinding.ActivityWordDetailedBinding
import com.pramod.dailyword.R
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.helper.*
import com.pramod.dailyword.helper.WindowPrefManager
import com.pramod.dailyword.ui.BaseActivity
import com.pramod.dailyword.util.CommonUtils
import kotlinx.android.synthetic.main.activity_word_list.*

class WordDetailedActivity : BaseActivity<ActivityWordDetailedBinding, WordDetailedViewModel>() {


    companion object {

        fun openActivity(context: Context, wordDate: String) {
            val intent = Intent(context, WordDetailedActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("WORD_DATE", wordDate)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }


        fun openActivity(context: Context, word: WordOfTheDay, option: ActivityOptions?) {
            val intent = Intent(context, WordDetailedActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("WORD", word)
            intent.putExtras(bundle)
            if (option != null &&
                WindowAnimationPrefManager.newInstance(context).isWindowAnimationEnabled()
            ) {
                context.startActivity(intent, option.toBundle())
            } else {
                context.startActivity(intent)
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_word_detailed

    override fun getViewModel(): WordDetailedViewModel {
        val word = intent.extras?.getSerializable("WORD") as WordOfTheDay?
        val wordDate = intent.extras?.getString("WORD_DATE")
        return ViewModelProviders.of(
            this,
            WordDetailedViewModel.Factory(application, word ?: WordOfTheDay(wordDate))
        ).get(WordDetailedViewModel::class.java)
    }

    override fun getBindingVariable(): Int = BR.wordDetailedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        //initTransitionAxis()
        initEnterAndReturnTransition()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onCreate(savedInstanceState)
        setUpToolbar()
        arrangeViewsAccordingToEdgeToEdge()
        setNestedScrollListener()
        setNavigateMW()
        showNativeAdDialogWithDelay()
        setUpWordOfTheDay()
    }

    private fun setUpToolbar() {
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.let {
            it.title = null
        }
        mBinding.toolbar.setNavigationIcon(R.drawable.ic_round_back_arrow)
        mBinding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setUpWordOfTheDay() {
        setUpExampleRecyclerView()
        setUpDefinationRecyclerView()
        mViewModel.wordOfTheDayLiveData.observe(this, {
            if (it != null) {
                Log.i("WORD OF THE DAY", Gson().toJson(it))
                invalidateOptionsMenu()
            }
        })
    }

    private fun setUpExampleRecyclerView() {
        val adapter = ExampleAdapter()
        mBinding.wordDetailedExamplesRecyclerview.adapter = adapter

        mViewModel.wordOfTheDayLiveData.observe(this, Observer {
            it?.let { word ->
                adapter.setColors(word.wordColor, word.wordDesaturatedColor)
                if (word.meanings != null) {
                    adapter.submitList(word.examples)
                }
            }
        })
    }

    private fun setUpDefinationRecyclerView() {
        val adapter = DefinationAdapter()
        mBinding.wordDetailedDefinationsRecyclerview.adapter = adapter

        mViewModel.wordOfTheDayLiveData.observe(this, Observer {
            it?.let { word ->
                adapter.setColors(word.wordColor, word.wordDesaturatedColor)
                if (word.meanings != null) {
                    adapter.submitList(word.meanings)
                }
            }
        })
    }

    private fun setNavigateMW() {
        mViewModel.navigateToMerriamWebster().observe(this, Observer {
            it.getContentIfNotHandled()?.let { url ->
                openWebsite(url)
            }
        })
    }

    override fun arrangeViewsForEdgeToEdge(view: View, insets: WindowInsetsCompat) {
        mBinding.appBar.setPadding(
            0, insets.systemWindowInsetTop, 0, 0
        )

        val paddingTop = insets.systemWindowInsetTop + mBinding.nestedScrollView.paddingTop
        val paddingBottom = insets.systemWindowInsetBottom

        mBinding.nestedScrollView.setPadding(
            0,
            paddingTop,
            0,
            paddingBottom
        )

        mBinding.swipeRefreshLayout.setProgressViewOffset(
            true,
            paddingTop,
            100 + paddingTop
        )

        /*val fabMarginBottom = mBinding.fabGotToMw.marginBottom + paddingBottom
        val layoutParam: CoordinatorLayout.LayoutParams =
            mBinding.fabGotToMw.layoutParams as CoordinatorLayout.LayoutParams
        layoutParam.setMargins(
            mBinding.fabGotToMw.marginLeft,
            mBinding.fabGotToMw.marginTop,
            mBinding.fabGotToMw.marginRight,
            fabMarginBottom
        )
        mBinding.fabGotToMw.layoutParams = layoutParam
*/
    }

    private fun arrangeViewsAccordingToEdgeToEdge() {
        if (!WindowPrefManager.newInstance(this).isEdgeToEdgeEnabled()) {
            val actionBarSize = CommonUtils.calculateActionBarHeight(this)
            mBinding.swipeRefreshLayout.setProgressViewOffset(
                true,
                actionBarSize,
                100 + actionBarSize
            )
        }


    }

    private fun initEnterAndReturnTransition() {

        findViewById<View>(android.R.id.content).transitionName =
            resources.getString(R.string.card_transition_name)
        window.sharedElementsUseOverlay = false
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())

        val enterTransition = MaterialContainerTransform().apply {
            setAllContainerColors(
                MaterialColors.getColor(findViewById(android.R.id.content), R.attr.colorSurface)
            )
            addTarget(android.R.id.content)
            excludeTarget(android.R.id.statusBarBackground, true)
            excludeTarget(android.R.id.navigationBarBackground, true)
            duration = 300
            pathMotion = ArcMotion()
            interpolator = FastOutSlowInInterpolator()
        }

        val returnTransition = MaterialContainerTransform().apply {
            setAllContainerColors(
                MaterialColors.getColor(findViewById(android.R.id.content), R.attr.colorSurface)
            )
            addTarget(android.R.id.content)
            excludeTarget(android.R.id.statusBarBackground, true)
            excludeTarget(android.R.id.navigationBarBackground, true)
            duration = 260
            pathMotion = ArcMotion()
            interpolator = FastOutSlowInInterpolator()
        }

        window.sharedElementEnterTransition = enterTransition
        window.sharedElementReturnTransition = returnTransition
    }

    private fun setNestedScrollListener() {
        mBinding.nestedScrollView.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            val distanceToCover =
                mBinding.txtViewWordOfTheDay.height + mBinding.txtViewWordOfTheDayDate.height
            mViewModel.setTitleVisibility(distanceToCover < oldScrollY)
            /*  if (oldScrollY < scrollY) {
                  mBinding.fabGotToMw.shrink()
              } else {
                  mBinding.fabGotToMw.extend()
              }*/
        }
    }

    private fun showNativeAdDialogWithDelay() {
        Handler().postDelayed({
            AdsManager.incrementCountAndShowNativeAdDialog(this)
        }, 1000)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.word_detail_menu, menu)
        Log.i("DATA", mViewModel.wordOfTheDayLiveData.value?.isBookmarked().toString())
        menu?.findItem(R.id.menu_bookmark)
            ?.setIcon(
                if (mViewModel.wordOfTheDayLiveData.value?.isBookmarked() == true
                ) R.drawable.ic_round_bookmark_24 else R.drawable.ic_baseline_bookmark_border_24
            )
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_share_word -> shareApp()
            R.id.menu_bookmark -> mViewModel.bookmark()
        }
        return super.onOptionsItemSelected(item)
    }

}
