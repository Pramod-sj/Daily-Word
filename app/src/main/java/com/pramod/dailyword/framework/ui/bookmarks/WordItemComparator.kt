package com.pramod.dailyword.framework.ui.bookmarks

import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil
import com.pramod.dailyword.business.domain.model.Word

object WordItemComparator : DiffUtil.ItemCallback<Word>() {
    override fun areItemsTheSame(oldItem: Word, newItem: Word): Boolean {
        return oldItem.date == newItem.date
    }

    override fun areContentsTheSame(oldItem: Word, newItem: Word): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: Word, newItem: Word): Any? {
        val bundle = Bundle()
        bundle.putString("DATE", newItem.date)
        bundle.putString("WORD", newItem.word)
        bundle.putStringArrayList(
                "DEFINATION",
                newItem.meanings?.let { ArrayList(it) } ?: arrayListOf())
        return bundle
    }
}