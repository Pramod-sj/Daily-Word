package com.pramod.dailyword.framework.ui.worddetails.viewholder

import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import androidx.lifecycle.Observer
import com.library.audioplayer.APEvent
import com.library.audioplayer.AudioPlayer
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ItemWdWordBasicInfoLayoutBinding
import com.pramod.dailyword.databinding.ItemWordDefinationLayoutBinding
import com.pramod.dailyword.framework.prefmanagers.ThemeManager
import com.pramod.dailyword.framework.ui.common.BaseViewHolder
import com.pramod.dailyword.framework.ui.common.SimpleListAdapter
import com.pramod.dailyword.framework.ui.common.bindingadapter.LottieUtils.animate
import com.pramod.dailyword.framework.ui.common.exts.changeLayersColor
import com.pramod.dailyword.framework.ui.worddetails.StringComparator
import com.pramod.dailyword.framework.ui.worddetails.WordDetailAdapterItemClickListener
import com.pramod.dailyword.framework.ui.worddetails.uimodel.WordDetailUiModel
import com.pramod.dailyword.framework.util.CalenderUtil
import com.pramod.dailyword.framework.util.CommonUtils

/**
 * Created by Pramod on 29,December,2021
 */
class WordTopCardViewHolder(
    private val binding: ItemWdWordBasicInfoLayoutBinding,
    private val listener: WordDetailAdapterItemClickListener,
    private val audioPlayer: AudioPlayer
) : BaseViewHolder<WordDetailUiModel>(binding) {

    val themeManager = ThemeManager(context)

    @ColorInt
    private var wordColor: Int? = null


    private val meaningAdapter by lazy {
        SimpleListAdapter(
            inflate = ItemWordDefinationLayoutBinding::inflate,
            itemComparator = StringComparator,
            onBind = { pos, rowBinding, data ->
                rowBinding.wordExampleDot.backgroundTintList =
                    wordColor?.let { ColorStateList.valueOf(it) }
                rowBinding.tvMeaningText.text = data
            }
        )
    }

    private val observer = { event: APEvent<Boolean> ->
        animate(binding.lottieSpeaker, event.peekContent())
    }

    private val themeChangeObserver = Observer { _: String ->
        binding.lottieSpeaker.post {
            binding.lottieSpeaker.changeLayersColor(R.color.app_icon_tint)
        }
    }

    init {
        binding.rvDefinations.adapter = meaningAdapter
    }

    override fun bind(data: WordDetailUiModel) {
        data as WordDetailUiModel.WordInfoTopCard
        wordColor = data.wordColor
        binding.tvWord.text = data.word
        with(binding.tvWordDateLabel) {
            setTextColor(data.wordColor)
            backgroundTintList = ColorStateList.valueOf(CommonUtils.changeAlpha(data.wordColor, 30))
            text = String.format(
                "%s %s",
                CalenderUtil.convertDateStringToSpecifiedDateString(
                    dateString = data.date,
                    dateFormat = CalenderUtil.DATE_FORMAT,
                    requiredDateFormat = CalenderUtil.DATE_WITH_YEAR_FORMAT_DISPLAY
                ), " - Merriam Webster Word"
            )
            setOnClickListener {
                listener.navigateToWeb(
                    String.format(
                        "%s/%s",
                        resources.getString(R.string.app_merriam_webster_icon_url),
                        data.date
                    )
                )
            }
        }
        binding.tvWordAttribute.text = data.attribute
        binding.tvWordPronounce.text = data.pronounce
        binding.lottieSpeaker.setOnClickListener {
            data.pronounceAudioUrl?.let { pronounceAudioUrl ->
                audioPlayer.play(pronounceAudioUrl)
            }
        }
        meaningAdapter.submitList(data.meanings)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        audioPlayer.audioPlaying.observeForever(observer)
        themeManager.liveData().observeForever(themeChangeObserver)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        audioPlayer.audioPlaying.removeObserver(observer)
        themeManager.liveData().removeObserver(themeChangeObserver)
    }

}