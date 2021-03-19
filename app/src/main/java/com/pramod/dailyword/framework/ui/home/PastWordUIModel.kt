package com.pramod.dailyword.framework.ui.home

import com.pramod.dailyword.business.domain.model.Word

data class PastWordUIModel(
    val day: String,
    val word: Word
)