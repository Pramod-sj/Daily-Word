package com.pramod.dailyword.framework.ui.worddetails

import android.app.Instrumentation
import android.content.Intent
import android.content.res.ColorStateList
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
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityWordDetailedBinding
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.haptics.HapticType
import com.pramod.dailyword.framework.helper.openGmail
import com.pramod.dailyword.framework.helper.openWebsite
import com.pramod.dailyword.framework.prefmanagers.ThemeManager
import com.pramod.dailyword.framework.transition.TransitionCallback
import com.pramod.dailyword.framework.transition.doOnViewLoaded
import com.pramod.dailyword.framework.transition.removeCallbacks
import com.pramod.dailyword.framework.ui.common.Action
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.Message
import com.pramod.dailyword.framework.ui.common.bindingadapter.ButtonBA
import com.pramod.dailyword.framework.ui.common.bindingadapter.ChipGroupBA
import com.pramod.dailyword.framework.ui.common.bindingadapter.OnChipClickListener
import com.pramod.dailyword.framework.ui.common.bindingadapter.OnChipViewMoreClickListener
import com.pramod.dailyword.framework.ui.common.exts.changeLayersColor
import com.pramod.dailyword.framework.ui.common.exts.getContextCompatColor
import com.pramod.dailyword.framework.ui.common.exts.setUpToolbar
import com.pramod.dailyword.framework.ui.common.exts.shareApp
import com.pramod.dailyword.framework.ui.common.exts.shouldShowSupportDevelopmentDialog
import com.pramod.dailyword.framework.ui.common.exts.showBottomSheet
import com.pramod.dailyword.framework.util.CalenderUtil
import com.pramod.dailyword.framework.util.CommonUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class WordDetailedActivity :
    BaseActivity<ActivityWordDetailedBinding, WordDetailedViewModel>(R.layout.activity_word_detailed) {

    override val viewModel: WordDetailedViewModel by viewModels()

    override val bindingVariable: Int = BR.wordDetailedViewModel

    override val screenName: String
        get() = if (viewModel.wordDate == null) "RandomWordDetailedActivity" else super.screenName

    @Inject
    lateinit var fbRemoteConfig: FBRemoteConfig

    private val exampleAdapter = ExampleAdapter()
    private val definitionAdapter = DefinitionAdapter()

    private val otherWordChipClickListener = object : OnChipClickListener {
        override fun onChipClick(text: String) {
            viewModel.navigator?.navigateToMerriamWebsterPage(text)
        }
    }

    private val thesaurusChipClickListener = object : OnChipClickListener {
        override fun onChipClick(text: String) {
            viewModel.navigator?.navigateToWeb("${resources.getString(R.string.google_search_url)}$text")
        }
    }

    private val viewMoreSynonyms = object : OnChipViewMoreClickListener {
        override fun onViewMoreClick(v: View) {
            viewModel.word.value?.synonyms.let { list ->
                viewModel.navigator?.navigateToShowSynonymsList(list)
            }
        }
    }

    private val viewMoreAntonyms = object : OnChipViewMoreClickListener {
        override fun onViewMoreClick(v: View) {
            viewModel.word.value?.antonyms.let { list ->
                viewModel.navigator?.navigateToShowAntonymsList(list)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportPostponeEnterTransition()
        initEnterAndReturnTransition()
        keepScreenOn()
        setUpToolbar(binding.toolbar, null, true)
        setupViews()
        observeWordData()
        setNestedScrollListener()
        setNavigateMW()
        handleNavigator()
        handleRippleAnimationForAudioEffect()
        shouldShowSupportDevelopmentDialog()
        adController.loadBanner(binding.frameAd1)
        adController.loadMediumBanner(binding.adPlaceholderMedium)
    }

    var job: Job? = null

    private fun setupViews() {
        binding.wordDetailedExamplesRecyclerview.adapter = exampleAdapter
        binding.wordDetailedDefinationsRecyclerview.adapter = definitionAdapter
        binding.lottieSpeaker.setOnClickListener {
            viewModel.word.value?.pronounceAudio?.let {
                hapticFeedbackManager.perform(HapticType.CLICK)
                viewModel.audioPlayer.play(it)
                job?.cancel()
                job = lifecycleScope.launch {
                    viewModel.audioPlayer.audioPlaying.asFlow()
                        .firstOrNull { !it.peekContent() } //wait till audio plays
                    delay(500L)
                    interstitialAdTracker.incrementActionCount()
                }
            }
        }
        binding.composeLoader.setViewTreeLifecycleOwner(this@WordDetailedActivity)
        binding.composeLoader.setContent { WordDetailShimmerLoadingScreen() }
    }

    private fun observeWordData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.wordAsFlow.collect { word ->
                    if (word != null) {
                        binding.nestedScrollView.isVisible = true
                        binding.composeLoader.isVisible = false
                        binding.swipeRefreshLayout.isRefreshing = false

                        // Set word color for various components
                        val color = getContextCompatColor(
                            if (ThemeManager.isNightModeActive(this@WordDetailedActivity)) word.wordDesaturatedColor
                            else word.wordColor
                        )
                        binding.wordColor = color
                        binding.executePendingBindings()

                        binding.txtViewWordOfTheDayDate.setTextColor(color)
                        binding.txtViewWordOfTheDayDate.backgroundTintList =
                            ColorStateList.valueOf(CommonUtils.changeAlpha(color, 30))

                        // Set text values directly
                        binding.txtViewWordOfTheDay.text = word.word
                        binding.txtViewWordOfTheDayDate.text = "${
                            CalenderUtil.convertDateStringToSpecifiedDateString(
                                word.date,
                                CalenderUtil.DATE_FORMAT,
                                CalenderUtil.DATE_WITH_YEAR_FORMAT_DISPLAY
                            )
                        } - Merriam Webster Word"
                        binding.tvWordAttribute.text = word.attribute
                        binding.tvWordPronounce.text = word.pronounce
                        binding.tvHowToUseTitle.text = "How to use ${word.word}"

                        binding.llDidYouKnowSection.isVisible = !word.didYouKnow.isNullOrEmpty()
                        binding.tvDidYouKnowSynopsis.text = word.didYouKnow

                        // Update Chips
                        binding.cardOtherWords.isGone = word.otherWords.orEmpty().isEmpty()
                        ChipGroupBA.addChips(
                            chipGroup = binding.chipGroupOtherWords,
                            chipTextList = word.otherWords,
                            chipColor = color,
                            onChipViewMoreClick = null,
                            onChipClickListener = otherWordChipClickListener,
                            chipShowViewMoreButton = false
                        )

                        binding.cardSynonyms.isGone = word.synonyms.orEmpty().isEmpty()
                        val synonyms = CommonUtils.getTopNItemFromList(word.synonyms, 6)
                        ChipGroupBA.addChips(
                            chipGroup = binding.chipGroupSynonyms,
                            chipTextList = synonyms,
                            chipColor = color,
                            onChipViewMoreClick = viewMoreSynonyms,
                            onChipClickListener = thesaurusChipClickListener,
                            chipShowViewMoreButton = word.synonyms.orEmpty().size > 6
                        )

                        binding.cardAntonyms.isGone = word.antonyms.orEmpty().isEmpty()
                        val antonyms = CommonUtils.getTopNItemFromList(word.antonyms, 6)
                        ChipGroupBA.addChips(
                            chipGroup = binding.chipGroupAntonyms,
                            chipTextList = antonyms,
                            chipColor = color,
                            onChipViewMoreClick = viewMoreAntonyms,
                            onChipClickListener = thesaurusChipClickListener,
                            chipShowViewMoreButton = word.antonyms.orEmpty().size > 6
                        )


                        // Update adapters
                        exampleAdapter.setColors(word.wordColor, word.wordDesaturatedColor)
                        definitionAdapter.setColors(word.wordColor, word.wordDesaturatedColor)
                        exampleAdapter.submitList(word.examples)
                        definitionAdapter.submitList(word.meanings)

                        binding.btnGoToMerriamWebster
                        ButtonBA.setButtonTextColor(
                            button = binding.btnGoToMerriamWebster,
                            word = word
                        )

                        binding.tvToolbarTitle.text = word.word

                        // Invalidate menu to update bookmark icon
                        invalidateOptionsMenu()

                        // Start postponed transition only after all data is set and views are ready
                        doOnViewLoaded(
                            binding.wordDetailedDefinationsRecyclerview,
                            binding.wordDetailedExamplesRecyclerview,
                            binding.chipGroupAntonyms,
                            binding.chipGroupSynonyms,
                            loadedCallback = { supportStartPostponedEnterTransition() }
                        )
                    }
                }
            }
        }

        viewModel.loadingLiveData.observe(this) { isLoading ->
            if (isLoading) {
                if (viewModel.word.value != null) {
                    binding.swipeRefreshLayout.isRefreshing = true
                    binding.nestedScrollView.isVisible = true
                    binding.composeLoader.isVisible = false
                } else {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.nestedScrollView.isVisible = false
                    binding.composeLoader.isVisible = true
                }
            } else {
                binding.swipeRefreshLayout.isRefreshing = false
                binding.composeLoader.isVisible = false
                binding.nestedScrollView.isVisible = true
            }
        }
    }

    private fun keepScreenOn() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.word_detail_menu, menu)
        viewModel.word.value?.let {
            menu?.findItem(R.id.menu_bookmark)
                ?.setIcon(
                    if (it.bookmarkedId != null
                    ) R.drawable.ic_round_bookmark_24 else R.drawable.ic_baseline_bookmark_border_24
                )
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_share_word -> {
                CommonUtils.viewToBitmap(binding.linearLayoutWordInfo)?.let {
                    shareApp(bitmap = it)
                } ?: shareApp()
            }

            R.id.menu_bookmark -> {
                viewModel.bookmark()
                hapticFeedbackManager.perform(HapticType.CLICK)
                interstitialAdTracker.incrementActionCount()
            }
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
