package com.pramod.dailyword.framework.ui.worddetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ItemWordDefinationLayoutBinding

class DefinitionAdapter(
    private var colorResId: Int? = null,
    private var desaturatedColorResId: Int? = null
) : ListAdapter<String, DefinitionAdapter.DefinationViewHolder>(StringComparator) {

    fun setColors(colorResId: Int?, desaturatedColorResId: Int?) {
        this.colorResId = colorResId
        this.desaturatedColorResId = desaturatedColorResId
    }


    class DefinationViewHolder(val binding: ItemWordDefinationLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefinationViewHolder {
        val binding: ItemWordDefinationLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_word_defination_layout,
            parent,
            false
        )
        return DefinationViewHolder(binding)
    }


    override fun onBindViewHolder(holder: DefinationViewHolder, position: Int) {
        holder.binding.colorResId = colorResId
        holder.binding.desaturatedColorResId = desaturatedColorResId
        holder.binding.srNo = position + 1
        holder.binding.defination = getItem(position)
        holder.binding.executePendingBindings()
    }



}