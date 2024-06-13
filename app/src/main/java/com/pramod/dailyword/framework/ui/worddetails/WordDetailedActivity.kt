package com.pramod.dailyword.framework.ui.worddetails

import android.app.Instrumentation
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.ArcMotion
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionSet
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.widget.NestedScrollView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityWordDetailedBinding
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.helper.openGmail
import com.pramod.dailyword.framework.helper.openWebsite
import com.pramod.dailyword.framework.prefmanagers.ThemeManager
import com.pramod.dailyword.framework.transition.TransitionCallback
import com.pramod.dailyword.framework.transition.doOnViewLoaded
import com.pramod.dailyword.framework.transition.removeCallbacks
import com.pramod.dailyword.framework.ui.common.Action
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.Message
import com.pramod.dailyword.framework.ui.common.exts.*
import com.pramod.dailyword.framework.util.CommonUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import timber.log.Timber
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
        shouldShowSupportDevelopmentDialog()
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
            doOnViewLoaded(
                binding.wordDetailedDefinationsRecyclerview,
                binding.wordDetailedExamplesRecyclerview,
                binding.chipGroupAntonyms,
                binding.chipGroupSynonyms,
                loadedCallback = { supportStartPostponedEnterTransition() }
            )
        }
    }

    private fun handleRippleAnimationForAudioEffect() {
        lifecycleScope.launch {
            themeManager.liveData().asFlow().collect {
                binding.lottieSpeaker.post {
                    binding.lottieSpeaker.changeLayersColor(R.color.app_icon_tint)
                }
            }
        }
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
        lifecycleScope.launchWhenCreated {
            viewModel.wordAsFlow.filterNotNull().collect { word ->
                adapter.setColors(word.wordColor, word.wordDesaturatedColor)
                if (word.meanings != null) {
                    adapter.submitList(word.examples)
                }
            }
        }
    }

    private fun setUpDefinitionRecyclerView() {
        val adapter = DefinitionAdapter()
        binding.wordDetailedDefinationsRecyclerview.adapter = adapter
        lifecycleScope.launchWhenCreated {
            viewModel.wordAsFlow.filterNotNull().collect { word ->
                adapter.setColors(word.wordColor, word.wordDesaturatedColor)
                if (word.meanings != null) {
                    adapter.submitList(word.meanings)
                }
            }
        }
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
                showBottomSheet(
                    title = title,
                    desc = desc,
                    positiveText = resources.getString(R.string.dialog_common_okay_btn)
                )
            }

            override fun navigateToMerriamWebsterPage(value: String) {
                try {
                    if (value.contains("-")) {
                        val word = value.split("-")[0]
                        openWebsite(resources.getString(R.string.google_search_url) + word)
                    } else {
                        throw Exception("Malformed other word:$value")
                    }
                } catch (e: Exception) {
                    viewModel.setMessage(
                        Message.SnackBarMessage(
                            message = String.format(
                                resources.getString(R.string.snack_bar_action_message),
                                e.message
                            ),
                            action = Action(resources.getString(R.string.snack_bar_action_report_btn)) {
                                openGmail(
                                    arrayOf(resources.getString(R.string.dev_email)),
                                    resources.getString(R.string.mail_report_issue_subject),//"Daily Word issue",
                                    String.format(
                                        resources.getString(R.string.mail_report_issue_body),
                                        e.message
                                    ),//"Something went wrong! Cause: ${e.message}"
                                )
                            }
                        )
                    )
                }
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Timber.i("onNewIntent:" + intent?.getStringExtra("WORD_DATE"))
        intent?.getStringExtra("WORD_DATE")?.let {
            viewModel.fetchWord(it)
        }
    }

    companion object {

        private const val EXTRA_WAS_BOOKMARK_STATUS_CHANGED = "was_bookmark_status_changed"

        fun wasBookmarkStatusChanged(resultCode: Int, data: Bundle?): Boolean {
            if (resultCode == RESULT_OK && data != null) {
                return data.getBoolean(EXTRA_WAS_BOOKMARK_STATUS_CHANGED, false)
            }
            return false
        }

    }
}
