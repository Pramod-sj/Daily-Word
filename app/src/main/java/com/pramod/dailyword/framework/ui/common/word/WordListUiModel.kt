package com.pramod.dailyword.framework.ui.common.word

import com.pramod.dailyword.business.domain.model.Word

sealed class WordListUiModel {
    data class WordItem(val index: Long, val word: Word) : WordListUiModel()
    data class AdItem(val adId: String) : WordListUiModel()
}
