package com.pramod.dailyword.ui.word_details

import com.pramod.dailyword.BR
import android.os.Bundle
import android.transition.ArcMotion
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.pramod.dailyword.databinding.ActivityWordDetailedBinding
import com.pramod.dailyword.R
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.helper.WindowPreferencesManager
import com.pramod.dailyword.ui.BaseActivity
import com.pramod.dailyword.util.CommonUtils

class WordDetailedActivity : BaseActivity<ActivityWordDetailedBinding, WordDetailedViewModel>() {

    override fun getLayoutId(): Int = R.layout.activity_word_detailed

    override fun getViewModel(): WordDetailedViewModel {
        val word = intent.extras!!.getSerializable("WORD") as WordOfTheDay

        return ViewModelProviders.of(this, WordDetailedViewModel.Factory(application, word))
            .get(WordDetailedViewModel::class.java)
    }

    override fun getBindingVariable(): Int = BR.wordDetailedViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        initEnterAndReturnTransition()
        super.onCreate(savedInstanceState)
        setUpToolbar()
        setNestedScrollListener()
        arrangeViewsAccordingToEdgeToEdge()
    }

    private fun setUpToolbar() {
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.let {
            it.title = null
        }
        mBinding.toolbar.setNavigationIcon(R.drawable.ic_round_back_arrow)
        mBinding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun arrangeViewsAccordingToEdgeToEdge() {
        if (WindowPreferencesManager.newInstance(this).isEdgeToEdgeEnabled()) {
            ViewCompat.setOnApplyWindowInsetsListener(
                mBinding.root
            ) { v, insets ->
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
                insets
            }
        }
    }

    private fun initEnterAndReturnTransition() {

        val enterTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            excludeTarget(android.R.id.statusBarBackground, true)
            excludeTarget(android.R.id.navigationBarBackground, true)
            duration = 300
            fadeMode = MaterialContainerTransform.FADE_MODE_OUT
            pathMotion = ArcMotion()
            interpolator = FastOutSlowInInterpolator()
            containerColor =
                CommonUtils.resolveAttrToColor(
                    this@WordDetailedActivity,
                    android.R.attr.windowBackground
                )
        }

        val returnTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            excludeTarget(android.R.id.statusBarBackground, true)
            excludeTarget(android.R.id.navigationBarBackground, true)
            duration = 250
            pathMotion = ArcMotion()
            fadeMode = MaterialContainerTransform.FADE_MODE_IN
            interpolator = FastOutSlowInInterpolator()
            containerColor =
                CommonUtils.resolveAttrToColor(
                    this@WordDetailedActivity,
                    android.R.attr.windowBackground
                )
        }


        findViewById<View>(android.R.id.content).transitionName = "CONTAINER"
        window.sharedElementEnterTransition = enterTransition
        window.sharedElementReturnTransition = returnTransition
        window.sharedElementsUseOverlay = false
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
    }

    private fun setNestedScrollListener() {
        mBinding.nestedScrollView.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            val distanceToCover = mBinding.txtViewWordOfTheDay.height
            Log.i("TEXTVIEW HEIGHT", distanceToCover.toString())
            mViewModel.setTitleVisibility(distanceToCover < oldScrollY)
        }
    }
}
