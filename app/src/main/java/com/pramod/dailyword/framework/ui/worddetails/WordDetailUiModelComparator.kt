package com.pramod.dailyword.framework.ui.worddetails

import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import com.pramod.dailyword.framework.ui.worddetails.uimodel.WordDetailUiModel

/**
 * Created by Pramod on 29,December,2021
 */
class WordDetailUiModelComparator : DiffUtil.ItemCallback<WordDetailUiModel>() {
    override fun areItemsTheSame(oldItem: WordDetailUiModel, newItem: WordDetailUiModel): Boolean {
        return oldItem.viewType == newItem.viewType
    }

    override fun areContentsTheSame(
        oldItem: WordDetailUiModel,
        newItem: WordDetailUiModel
    ): Boolean {
        return oldItem == newItem
    }
}

val AsyncWordDetailUiModelComparator =
    AsyncDifferConfig.Builder(WordDetailUiModelComparator()).build()