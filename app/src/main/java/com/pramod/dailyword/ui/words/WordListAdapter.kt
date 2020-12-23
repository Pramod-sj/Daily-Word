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
    val itemClickCallback: ((pos: Int, word: WordOfTheDay) -> Unit)? = null,
) :
    PagedListAdapter<WordOfTheDay, WordListAdapter.WordViewHolder>(diffCallback = WordDiffCallback) {
    private var canStartActivity = true

    fun setCanStartActivity(canStart: Boolean) {
        canStartActivity = canStart
    }

    inner class WordViewHolder(private val binding: ItemWordListLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(word: WordOfTheDay?) {
            binding.wordOfTheDay = word
            binding.itemWordListCardView.setOnClickListener {
                if (canStartActivity) {
                    canStartActivity = false;
                    itemClickCallback?.invoke(bindingAdapterPosition, word!!)
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
        holder.bind(getItem(position))
    }


}