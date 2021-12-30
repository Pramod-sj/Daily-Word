package com.pramod.dailyword.framework.ui.worddetails

import android.content.Context
import com.pramod.dailyword.R
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.framework.prefmanagers.ThemeManager
import com.pramod.dailyword.framework.ui.worddetails.uimodel.WordDetailUiModel
import com.pramod.dailyword.framework.util.CommonUtils
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

/**
 * Created by Pramod on 29,December,2021
 */
@ActivityScoped
class UiMapper @Inject constructor(
    @ActivityContext private val context: Context
) {

    fun toUiModels(word: Word): List<WordDetailUiModel> {
        val wordColorInt = CommonUtils.getColor(
            context = context,
            if (ThemeManager.isNightModeActive(context)) word.wordDesaturatedColor
            else word.wordColor
        )
        return mutableListOf(
            WordDetailUiModel.WordInfoTopCard(
                date = word.date,
                wordColor = wordColorInt,
                meanings = word.meanings,
                word = word.word,
                attribute = word.attribute,
                pronounce = word.pronounce,
                pronounceAudioUrl = word.pronounceAudio,
            ),
            WordDetailUiModel.ExampleCard(
                word = word.word,
                wordColor = wordColorInt,
                examples = word.examples,
            )
        ).apply {
            if (!word.synonyms.isNullOrEmpty()) {
                add(
                    WordDetailUiModel.ChipsCard(
                        title = context.getString(R.string.synonyms),
                        infoHint = context.getString(R.string.syn_info),
                        chipTexts = word.synonyms,
                        wordColor = wordColorInt
                    )
                )
            }

            if (!word.antonyms.isNullOrEmpty()) {
                add(
                    WordDetailUiModel.ChipsCard(
                        title = context.getString(R.string.antonyms),
                        infoHint = context.getString(R.string.ant_info),
                        chipTexts = word.antonyms,
                        wordColor = wordColorInt
                    )
                )
            }
        }
    }
}