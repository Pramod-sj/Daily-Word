package com.pramod.dailyword.framework.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pramod.dailyword.R
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.databinding.ItemPastWordLayoutBinding

class PastWordAdapter(
    val onItemClickCallback: (Int, Word) -> Unit
) : ListAdapter<Word, PastWordAdapter.WordViewHolder>(diffCallback) {
    private var canStartActivity = true

    fun setCanStartActivity(canStart: Boolean) {
        canStartActivity = canStart
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val binding: ItemPastWordLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_past_word_layout,
            parent,
            false
        )

        return WordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.binding.word = getItem(position)
        holder.binding.executePendingBindings()
    }


    inner class WordViewHolder(val binding: ItemPastWordLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            setUpListener()
        }

        private fun setUpListener() {
            binding.root.setOnClickListener {
                if (canStartActivity) {
                    canStartActivity = false;
                    onItemClickCallback.invoke(
                        bindingAdapterPosition,
                        getItem(bindingAdapterPosition)
                    )
                }
            }
        }
    }

    fun canStart() = canStartActivity


    companion object {
        private val diffCallback =
            object : DiffUtil.ItemCallback<Word>() {
                override fun areItemsTheSame(
                    oldItem: Word,
                    newItem: Word
                ): Boolean {
                    return oldItem.date == newItem.date
                }

                override fun areContentsTheSame(
                    oldItem: Word,
                    newItem: Word
                ): Boolean {
                    return oldItem == newItem
                }

                override fun getChangePayload(oldItem: Word, newItem: Word): Any? {
                    val bundle = Bundle()
                    bundle.putSerializable("date", newItem.date)
                    bundle.putSerializable("word", newItem.word)
                    bundle.putBoolean("isSeen", newItem.isSeen)
                    return bundle
                }

            }
    }

}

