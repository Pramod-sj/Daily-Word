package com.pramod.dailyword.ui.recapwords

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ItemRecapWordLayoutBinding
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.ui.words.WordDiffCallback

class RecapWordAdapter(
    private val itemClickCallback: ((pos: Int, word: WordOfTheDay) -> Unit)? = null
) :
    ListAdapter<WordOfTheDay, RecapWordAdapter.RecapWordViewHolder>(WordDiffCallback) {
    inner class RecapWordViewHolder(val binding: ItemRecapWordLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.itemWordListCardView.setOnClickListener {
                itemClickCallback?.invoke(bindingAdapterPosition, getItem(bindingAdapterPosition))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecapWordViewHolder {
        val binding: ItemRecapWordLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_recap_word_layout,
            parent,
            false
        )
        return RecapWordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecapWordViewHolder, position: Int) {
        holder.binding.wordOfTheDay = getItem(position)
        holder.binding.executePendingBindings()
    }
}