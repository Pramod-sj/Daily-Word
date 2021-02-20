package com.pramod.dailyword.framework.ui.recap

import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil
import com.google.gson.Gson
import com.pramod.dailyword.business.domain.model.Word

object WordDiffCallback : DiffUtil.ItemCallback<Word>() {
    override fun areItemsTheSame(
        oldItem: Word,
        newItem: Word
    ): Boolean {
        return oldItem.word == newItem.word
    }

    override fun areContentsTheSame(
        oldItem: Word,
        newItem: Word
    ): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: Word, newItem: Word): Any? {
        val bundle = Bundle()
        if (oldItem.date != newItem.date) {
            bundle.putString("DATE", newItem.date)
        }
        if (oldItem.word != newItem.word) {
            bundle.putString("WORD", newItem.word)
        }
        if (Gson().toJson(oldItem.meanings) != Gson().toJson(newItem.meanings)) {
            val array = arrayListOf<String>()
            newItem.meanings?.let {
                array.addAll(it.toMutableList())
            }
            bundle.putStringArrayList(
                "DEFINATION",
                array
            )
        }
        return bundle
    }

}