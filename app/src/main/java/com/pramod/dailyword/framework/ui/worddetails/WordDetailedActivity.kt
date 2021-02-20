package com.pramod.dailyword.framework.ui.worddetails

import android.os.Bundle
import android.transition.ArcMotion
import android.view.*
import androidx.activity.viewModels
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.pramod.dailyword.BR
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ActivityWordDetailedBinding
import com.pramod.dailyword.databinding.BottomSheetChipLayoutBinding
import com.pramod.dailyword.framework.helper.openWebsite
import com.pramod.dailyword.framework.prefmanagers.ThemeManager
import com.pramod.dailyword.framework.ui.common.BaseActivity
import com.pramod.dailyword.framework.ui.common.bindingadapter.OnChipClickListener
import com.pramod.dailyword.framework.ui.common.exts.changeLayersColor
import com.pramod.dailyword.framework.ui.common.exts.getContextCompatColor
import com.pramod.dailyword.framework.ui.common.exts.shareApp
import com.pramod.dailyword.framework.ui.common.exts.showBottomSheet
import com.pramod.dailyword.framework.util.CommonUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class WordDetailedActivity : BaseActivity<ActivityWordDetailedBinding, WordDetailedViewModel>() {

    override val layoutId: Int = R.layout.activity_word_detailed
    override val viewModel: WordDetailedViewModel by viewModels()
    override val bindingVariable: Int = BR.wordDetailedViewModel


    /* companion object {

         fun openActivity(context: Context, wordDate: String?, option: ActivityOptions?) {
             val intent = Intent(context, WordDetailedActivity::class.java)
             val bundle = Bundle()
             bundle.putSerializable("WORD_DATE", wordDate)
             intent.putExtras(bundle)
             intent.putExtras(bundle)
             if (option != null &&
                 WindowAnimationPrefManager.newInstance(context).isWindowAnimationEnabled()
             ) {
                 context.startActivity(intent, option.toBundle())
             } else {
                 context.startActivity(intent)
             }
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
     }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        initEnterAndReturnTransition()
        keepScreenOn()
        super.onCreate(savedInstanceState)
        setUpToolbar()
        setNestedScrollListener()
        setNavigateMW()
        invalidateOptionMenuWhenWordAvailable()
        setWordColor()
        setUpExampleRecyclerView()
        setUpDefinationRecyclerView()
        handleNavigator()
        handleRippleAnimationForAudioEffect()
    }

    private fun keepScreenOn() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }


    private fun setWordColor() {
        mViewModel.word.observe(this) {
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


        themeManager.liveData().observe(this) {
            binding.lottieSpeaker.post {
                binding.lottieSpeaker.changeLayersColor(R.color.app_icon_tint)
            }
        }

        mViewModel.isAudioPronouncing.observe(this) {
            if (it) {
                binding.lottieSpeaker.playAnimation()
            } else {
                binding.lottieSpeaker.cancelAnimation()
            }

        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.title = null
        }
        binding.toolbar.setNavigationIcon(R.drawable.ic_round_back_arrow)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun invalidateOptionMenuWhenWordAvailable() {
        mViewModel.word.observe(this, {
            if (it != null) {
                invalidateOptionsMenu()
            }
        })
    }

    private fun setUpExampleRecyclerView() {
        val adapter = ExampleAdapter()
        binding.wordDetailedExamplesRecyclerview.adapter = adapter

        mViewModel.word.observe(this, Observer {
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
        binding.wordDetailedDefinationsRecyclerview.adapter = adapter

        mViewModel.word.observe(this, {
            it?.let { word ->
                adapter.setColors(word.wordColor, word.wordDesaturatedColor)
                if (word.meanings != null) {
                    adapter.submitList(word.meanings)
                }
            }
        })
    }

    private fun handleNavigator() {
        mViewModel.navigator = object : WordDetailNavigator {
            override fun navigateToShowSynonymsList(list: List<String>?) {
                list?.let {
                    showBottomSheetListOfChips(resources.getString(R.string.synonyms), it)
                }
            }

            override fun navigateToShowAntonymsList(list: List<String>?) {
                list?.let {
                    showBottomSheetListOfChips(resources.getString(R.string.antonyms), it)
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
        mViewModel.navigateToMerriamWebster().observe(this, Observer {
            it.getContentIfNotHandled()?.let { url ->
                openWebsite(url)
            }
        })
    }

    private fun initEnterAndReturnTransition() {

        findViewById<View>(android.R.id.content).transitionName =
            resources.getString(R.string.card_transition_name)
        window.sharedElementsUseOverlay = false
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())

        val enterTransition = MaterialContainerTransform().apply {
            excludeTarget(android.R.id.statusBarBackground, true)
            excludeTarget(android.R.id.navigationBarBackground, true)
            setAllContainerColors(
                MaterialColors.getColor(findViewById(android.R.id.content), R.attr.colorSurface)
            )
            addTarget(android.R.id.content)
            duration = 300
            pathMotion = ArcMotion()
            interpolator = FastOutSlowInInterpolator()
        }

        val returnTransition = MaterialContainerTransform().apply {
            excludeTarget(android.R.id.statusBarBackground, true)
            excludeTarget(android.R.id.navigationBarBackground, true)
            setAllContainerColors(
                MaterialColors.getColor(findViewById(android.R.id.content), R.attr.colorSurface)
            )
            addTarget(android.R.id.content)
            duration = 200
            pathMotion = ArcMotion()
            interpolator = FastOutSlowInInterpolator()
        }

        window.sharedElementEnterTransition = enterTransition
        window.sharedElementReturnTransition = returnTransition
    }

    private fun setNestedScrollListener() {
        binding.nestedScrollView.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            val distanceToCover =
                binding.txtViewWordOfTheDay.height + binding.txtViewWordOfTheDayDate.height
            mViewModel.setTitleVisibility(distanceToCover < oldScrollY)
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
                if (mViewModel.word.value?.bookmarkedId != null
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
            R.id.menu_bookmark -> mViewModel.bookmark()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showBottomSheetListOfChips(title: String, list: List<String>) {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.AppTheme_BottomSheetDialog)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val binding = DataBindingUtil.inflate<BottomSheetChipLayoutBinding>(
            LayoutInflater.from(this),
            R.layout.bottom_sheet_chip_layout,
            null,
            false
        )
        binding.title = title
        binding.listData = list
        binding.onChipClickListener = object : OnChipClickListener {
            override fun onChipClick(text: String) {
                val url = resources.getString(R.string.google_search_url) + text
                mViewModel.navigator?.navigateToWeb(url)
            }
        }
        binding.executePendingBindings()
        bottomSheetDialog.setContentView(binding.root)
        bottomSheetDialog.show()
    }
}
