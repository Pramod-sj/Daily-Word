package com.pramod.dailyword.ui.word_details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ItemChipLayoutBinding

class ThesaurusAdapter(
    val itemClickCallback: ((String) -> Unit)? = null
) :
    ListAdapter<String, ThesaurusAdapter.ViewHolder>(StringDiffCallback) {
    inner class ViewHolder(val binding: ItemChipLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.chip.setOnClickListener {
                itemClickCallback?.invoke(getItem(bindingAdapterPosition))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemChipLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_chip_layout,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.chip.text = getItem(position)
        holder.binding.executePendingBindings()
    }
}