package com.pramod.dailyword.framework.ui.worddetails

import android.app.Instrumentation
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.ArcMotion
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionSet
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.widget.NestedScrollView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Observer
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityWordDetailedBinding
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.helper.openWebsite
import com.pramod.dailyword.framework.prefmanagers.ThemeManager
import com.pramod.dailyword.framework.transition.TransitionCallback
import com.pramod.dailyword.framework.transition.doOnViewLoaded
import com.pramod.dailyword.framework.transition.removeCallbacks
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.exts.*
import com.pramod.dailyword.framework.util.CommonUtils
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class WordDetailedActivity :
    BaseActivity<ActivityWordDetailedBinding, WordDetailedViewModel>(R.layout.activity_word_detailed) {

    override val viewModel: WordDetailedViewModel by viewModels()

    override val bindingVariable: Int = BR.wordDetailedViewModel

    @Inject
    lateinit var fbRemoteConfig: FBRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindAdsInfo()
        supportPostponeEnterTransition()
        initEnterAndReturnTransition()
        keepScreenOn()
        setUpToolbar(binding.toolbar, null, true)
        setNestedScrollListener()
        setNavigateMW()
        invalidateOptionMenuWhenWordAvailable()
        setWordColor()
        setUpExampleRecyclerView()
        setUpDefinitionRecyclerView()
        handleNavigator()
        handleRippleAnimationForAudioEffect()
        val start = Calendar.getInstance().timeInMillis
        doOnViewLoaded(
            binding.wordDetailedDefinationsRecyclerview,
            binding.wordDetailedExamplesRecyclerview,
            binding.chipGroupAntonyms,
            binding.chipGroupSynonyms,
            loadedCallback = {
                Log.i(TAG, "onGlobalLayout: " + (Calendar.getInstance().timeInMillis - start))
                supportStartPostponedEnterTransition()
            }
        )
    }

    private fun bindAdsInfo() {
        binding.setVariable(BR.adsEnabled, fbRemoteConfig.isAdsEnabled())
    }

    private fun keepScreenOn() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun setWordColor() {
        viewModel.word.observe(this) {
            it?.let { word ->
                binding.wordColor =
                    getContextCompatColor(
                        if (ThemeManager.isNightModeActive(this)) word.wordDesaturatedColor
                        else word.wordColor
                    )
            }
        }
    }

    private fun handleRippleAnimationForAudioEffect() {

        themeManager.liveData().observe(this, object : Observer<String> {
            override fun onChanged(t: String?) {
                binding.lottieSpeaker.post {
                    binding.lottieSpeaker.changeLayersColor(R.color.app_icon_tint)
                }
            }
        })

    }

    private fun invalidateOptionMenuWhenWordAvailable() {
        viewModel.word.observe(this, {
            if (it != null) {
                invalidateOptionsMenu()
            }
        })
    }

    private fun setUpExampleRecyclerView() {
        val adapter = ExampleAdapter()
        binding.wordDetailedExamplesRecyclerview.adapter = adapter
        viewModel.word.observe(this, {
            it?.let { word ->
                adapter.setColors(word.wordColor, word.wordDesaturatedColor)
                if (word.meanings != null) {
                    adapter.submitList(word.examples)
                }
            }
        })
    }

    private fun setUpDefinitionRecyclerView() {
        val adapter = DefinitionAdapter()
        binding.wordDetailedDefinationsRecyclerview.adapter = adapter

        viewModel.word.observe(this, {
            it?.let { word ->
                adapter.setColors(word.wordColor, word.wordDesaturatedColor)
                if (word.meanings != null) {
                    adapter.submitList(word.meanings)
                }
            }
        })
    }

    private fun handleNavigator() {
        viewModel.navigator = object : WordDetailNavigator {
            override fun navigateToShowSynonymsList(list: List<String>?) {
                list?.let {
                    ChipListDialogFragment.show(
                        resources.getString(R.string.synonyms),
                        it,
                        supportFragmentManager
                    )
                }
            }

            override fun navigateToShowAntonymsList(list: List<String>?) {
                list?.let {
                    ChipListDialogFragment.show(
                        resources.getString(R.string.antonyms),
                        it,
                        supportFragmentManager
                    )
                }
            }

            override fun navigateToWeb(url: String) {
                openWebsite(url)
            }

            override fun navigateToShowThesaurusInfo(title: String, desc: String) {
                showBottomSheet(title, desc, positiveText = "Okay")
            }

        }
    }

    private fun setNavigateMW() {
        viewModel.navigateToMerriamWebster().observe(this, Observer {
            it.getContentIfNotHandled()?.let { url ->
                openWebsite(url)
            }
        })
    }

    private fun initEnterAndReturnTransition() {

        findViewById<View>(android.R.id.content).transitionName =
            intent.extras?.getString("WORD_DATE")

        window.allowEnterTransitionOverlap = false
        window.allowReturnTransitionOverlap = false
        window.sharedElementsUseOverlay = false

        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())


        window.sharedElementEnterTransition =
            TransitionSet()
                .addTransition(MaterialContainerTransform().apply {
                    excludeTarget(android.R.id.statusBarBackground, true)
                    excludeTarget(android.R.id.navigationBarBackground, true)

                    setAllContainerColors(
                        MaterialColors.getColor(
                            findViewById(android.R.id.content),
                            R.attr.colorSurface
                        )
                    )
                    addTarget(android.R.id.content)
                    duration = 300
                    pathMotion = ArcMotion()
                    interpolator = FastOutSlowInInterpolator()
                })
                .addTransition(Fade().apply {
                    excludeTarget(android.R.id.statusBarBackground, true)
                    excludeTarget(android.R.id.navigationBarBackground, true)
                    interpolator = FastOutSlowInInterpolator()
                    duration = 300
                })

        window.sharedElementReturnTransition = MaterialContainerTransform().apply {

            excludeTarget(android.R.id.statusBarBackground, true)
            excludeTarget(android.R.id.navigationBarBackground, true)

            setAllContainerColors(
                MaterialColors.getColor(findViewById(android.R.id.content), R.attr.colorSurface)
            )
            addTarget(android.R.id.content)
            duration = 250
            pathMotion = ArcMotion()
            interpolator = FastOutSlowInInterpolator()
        }

        window.sharedElementEnterTransition.addListener(object : TransitionCallback() {
            override fun onTransitionEnd(transition: Transition) {
                super.onTransitionEnd(transition)
                removeCallbacks(this)
            }
        })

        window.sharedElementReturnTransition.addListener(object : TransitionCallback() {
            override fun onTransitionEnd(transition: Transition) {
                super.onTransitionEnd(transition)
                removeCallbacks(this)
            }
        })
    }

    private fun setNestedScrollListener() {
        binding.nestedScrollView.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            val distanceToCover =
                binding.txtViewWordOfTheDay.height + binding.txtViewWordOfTheDayDate.height
            viewModel.setTitleVisibility(distanceToCover < oldScrollY)
            /*  if (oldScrollY < scrollY) {
                  mBinding.fabGotToMw.shrink()
              } else {
                  mBinding.fabGotToMw.extend()
              }*/
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.word_detail_menu, menu)
        menu?.findItem(R.id.menu_bookmark)
            ?.setIcon(
                if (viewModel.word.value?.bookmarkedId != null
                ) R.drawable.ic_round_bookmark_24 else R.drawable.ic_baseline_bookmark_border_24
            )
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_share_word -> {
                CommonUtils.viewToBitmap(binding.linearLayoutWordInfo)?.let {
                    shareApp(bitmap = it)
                } ?: shareApp()
            }
            R.id.menu_bookmark -> viewModel.bookmark()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && !isFinishing) {
            Instrumentation().callActivityOnSaveInstanceState(this, Bundle())
        }
        super.onStop()
    }


    override fun finishAfterTransition() {
        setResult()
        super.finishAfterTransition()
    }

    private fun setResult() {
        val data = Intent()
        data.putExtra(EXTRA_WAS_BOOKMARK_STATUS_CHANGED, viewModel.isBookmarkStatusChanged)
        setResult(RESULT_OK, data)
    }

    companion object {

        private const val EXTRA_WAS_BOOKMARK_STATUS_CHANGED = "was_bookmark_status_changed"

        fun wasBookmarkStatusChanged(resultCode: Int, data: Bundle?): Boolean {
            if (resultCode == RESULT_OK && data != null) {
                return data.getBoolean(EXTRA_WAS_BOOKMARK_STATUS_CHANGED, false);
            }
            return false
        }

    }
}
