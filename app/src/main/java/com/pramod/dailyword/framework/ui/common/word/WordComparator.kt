package com.pramod.dailyword.framework.ui.common.word

import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil

object WordComparator : DiffUtil.ItemCallback<WordListUiModel>() {
    override fun areItemsTheSame(
        oldItem: WordListUiModel,
        newItem: WordListUiModel
    ): Boolean {

        return if (oldItem is WordListUiModel.WordItem && newItem is WordListUiModel.WordItem) {
            oldItem.word.date == newItem.word.date
        } else if (oldItem is WordListUiModel.AdItem && newItem is WordListUiModel.AdItem) {
            oldItem.adId == newItem.adId
        } else true
    }

    override fun areContentsTheSame(
        oldItem: WordListUiModel,
        newItem: WordListUiModel
    ): Boolean {
        return if (oldItem is WordListUiModel.WordItem && newItem is WordListUiModel.WordItem) {
            oldItem == newItem
        } else if (oldItem is WordListUiModel.AdItem && newItem is WordListUiModel.AdItem) {
            oldItem == newItem
        } else true
    }

    override fun getChangePayload(oldItem: WordListUiModel, newItem: WordListUiModel): Any? {
        return if (oldItem is WordListUiModel.WordItem && newItem is WordListUiModel.WordItem) {
            val wordOfTheDay = newItem.word
            val bundle = Bundle()
            bundle.putString("DATE", wordOfTheDay.date)
            bundle.putString("WORD", wordOfTheDay.word)
            bundle.putStringArrayList(
                "DEFINATION",
                wordOfTheDay.meanings?.let { ArrayList(it) } ?: arrayListOf())
            bundle
        } else if (oldItem is WordListUiModel.AdItem && newItem is WordListUiModel.AdItem) {
            null
        } else {
            null
        }
    }

}