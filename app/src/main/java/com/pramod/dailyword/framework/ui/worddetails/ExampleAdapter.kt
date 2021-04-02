package com.pramod.dailyword.framework.ui.worddetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ItemWordExampleLayoutBinding

class ExampleAdapter(
    private var colorResId: Int? = null,
    private var desaturatedColorResId: Int? = null
) : ListAdapter<String, ExampleAdapter.ExampleViewHolder>(StringComparator) {

    fun setColors(colorResId: Int?, desaturatedColorResId: Int?) {
        this.colorResId = colorResId;
        this.desaturatedColorResId = desaturatedColorResId;
    }

    class ExampleViewHolder(val binding: ItemWordExampleLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExampleViewHolder {
        val binding: ItemWordExampleLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_word_example_layout,
            parent,
            false
        )
        return ExampleViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ExampleViewHolder, position: Int) {
        holder.binding.colorResId = colorResId
        holder.binding.desaturatedColorResId = desaturatedColorResId
        holder.binding.srNo = position + 1
        holder.binding.example = getItem(position)
        holder.binding.executePendingBindings()
    }
}