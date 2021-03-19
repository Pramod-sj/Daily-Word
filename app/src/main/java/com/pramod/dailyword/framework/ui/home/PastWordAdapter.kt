package com.pramod.dailyword.framework.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pramod.dailyword.R
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.databinding.ItemPastWordLayoutBinding

class PastWordAdapter(
    val onItemClickCallback: (Int, Word) -> Unit
) : ListAdapter<PastWordUIModel, PastWordAdapter.WordViewHolder>(PastUIModelComparator) {
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
        val word = getItem(position)
        holder.binding.root.transitionName = word.word.date
        holder.binding.pastWordUIModel = word
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
                        getItem(bindingAdapterPosition).word
                    )
                }
            }
        }
    }

    fun canStart() = canStartActivity

    object PastUIModelComparator : DiffUtil.ItemCallback<PastWordUIModel>() {
        override fun areItemsTheSame(oldItem: PastWordUIModel, newItem: PastWordUIModel): Boolean {
            return oldItem.day == newItem.day
        }

        override fun areContentsTheSame(
            oldItem: PastWordUIModel,
            newItem: PastWordUIModel
        ): Boolean {
            return oldItem == newItem
        }


        override fun getChangePayload(oldItem: PastWordUIModel, newItem: PastWordUIModel): Any {
            return bundleOf(
                "day" to newItem.day,
                "word" to newItem.word
            )
        }

    }

}


