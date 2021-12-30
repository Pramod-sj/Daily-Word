package com.pramod.dailyword.framework.ui.worddetails.uimodel

import androidx.annotation.ColorInt
import androidx.annotation.IntRange

/**
 * Created by Pramod on 29,December,2021
 */
sealed class WordDetailUiModel(val viewType: WordDetailUiModelViewType) {

    data class WordInfoTopCard(
        val word: String?,
        val attribute: String?,
        val date: String?,
        val pronounce: String?,
        val pronounceAudioUrl: String?,
        @ColorInt val wordColor: Int,
        val meanings: List<String>?
    ) : WordDetailUiModel(WordDetailUiModelViewType.WORD_INFO_TOP_CARD)

    data class ExampleCard(
        val word: String?,
        @ColorInt val wordColor: Int,
        val examples: List<String>?
    ) : WordDetailUiModel(WordDetailUiModelViewType.EXAMPLE_CARD)

    data class DidYouKnowCard(val didYouKnowCard: String) :
        WordDetailUiModel(WordDetailUiModelViewType.DID_YOU_KNOW)

    data class ChipsCard(
        val title: String,
        val chipTexts: List<String>,
        val infoHint: String,
        @ColorInt val wordColor: Int,
    ) : WordDetailUiModel(WordDetailUiModelViewType.CHIPS_CARD) //Synonyms,Antonyms and Other words
}

enum class WordDetailUiModelViewType(val type: Int) {
    WORD_INFO_TOP_CARD(1),
    EXAMPLE_CARD(2),
    DID_YOU_KNOW(5),
    CHIPS_CARD(3);

    companion object {
        fun fromType(@IntRange(from = 1, to = 6) type: Int) =
            WordDetailUiModelViewType.values().first { it.type == type }
    }
}
