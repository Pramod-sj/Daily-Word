package com.pramod.dailyword.ui.words

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ItemNetworkStateLayoutBinding
import com.pramod.dailyword.databinding.ItemWordListLayoutBinding
import com.pramod.dailyword.db.model.NetworkState
import com.pramod.dailyword.db.model.NetworkState.Companion.LOADED
import com.pramod.dailyword.db.model.Status
import com.pramod.dailyword.db.model.WordOfTheDay
import java.lang.IllegalArgumentException


class WordListAdapter(
    val itemClickCallback: ((pos: Int, word: WordOfTheDay) -> Unit)? = null
) :
    PagedListAdapter<WordOfTheDay, WordListAdapter.WordViewHolder>(diffCallback) {
    private var canStartActivity = false

    fun setCanStartActivity(canStart: Boolean) {
        canStartActivity = canStart
    }

    inner class WordViewHolder(private val binding: ItemWordListLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, word: WordOfTheDay?) {
            binding.wordOfTheDay = word
            binding.itemWordListCardView.setOnClickListener {
                if (canStartActivity) {
                    canStartActivity = false;
                    itemClickCallback?.invoke(position, word!!)
                }
            }
            binding.executePendingBindings()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        return WordViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_word_list_layout,
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(position, getItem(position))
    }

    companion object {
        private val diffCallback =
            object : DiffUtil.ItemCallback<WordOfTheDay>() {
                override fun areItemsTheSame(
                    oldItem: WordOfTheDay,
                    newItem: WordOfTheDay
                ): Boolean {
                    return oldItem.id == newItem.id
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
    }


}