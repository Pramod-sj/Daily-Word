package com.pramod.dailyword.ui.words

import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil
import com.google.gson.Gson
import com.pramod.dailyword.db.model.WordOfTheDay

object WordDiffCallback : DiffUtil.ItemCallback<WordOfTheDay>() {
    override fun areItemsTheSame(
        oldItem: WordOfTheDay,
        newItem: WordOfTheDay
    ): Boolean {
        return oldItem.word == newItem.word
    }

    override fun areContentsTheSame(
        oldItem: WordOfTheDay,
        newItem: WordOfTheDay
    ): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: WordOfTheDay, newItem: WordOfTheDay): Any? {
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