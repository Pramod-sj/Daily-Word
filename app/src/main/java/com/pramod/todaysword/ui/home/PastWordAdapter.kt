package com.pramod.todaysword.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.pramod.todaysword.R
import com.pramod.todaysword.databinding.ItemPastWordLayoutBinding
import com.pramod.todaysword.db.model.WordOfTheDay

class PastWordAdapter(
    val onItemClickCallback: (Int, WordOfTheDay) -> Unit
) : RecyclerView.Adapter<PastWordAdapter.WordViewHolder>() {
    private val words: MutableList<WordOfTheDay> = ArrayList()

    fun setWords(words: List<WordOfTheDay>) {
        val result = DiffUtil.calculateDiff(WordDiffCallback(this.words, words))
        result.dispatchUpdatesTo(this)
        this.words.clear()
        this.words.addAll(words)
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

    override fun getItemCount(): Int {
        return words.size
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.binding.wordOfTheDay = words[position]
        holder.binding.position = position
        holder.binding.executePendingBindings()
    }


    inner class WordViewHolder(val binding: ItemPastWordLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.cardViewRootItemLayout.setOnClickListener {
                onItemClickCallback.invoke(
                    layoutPosition,
                    words[layoutPosition]
                )
            }
        }
    }

    class WordDiffCallback(
        private val oldList: List<WordOfTheDay>?,
        private val newList: List<WordOfTheDay>?
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList?.size ?: 0

        override fun getNewListSize(): Int = newList?.size ?: 0

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList!![oldItemPosition].date == newList!![newItemPosition].date

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList!![oldItemPosition] == newList!![newItemPosition]
    }


}

