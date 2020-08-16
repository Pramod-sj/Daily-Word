package com.pramod.dailyword.ui.word_details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ItemWordDefinationLayoutBinding
import com.pramod.dailyword.databinding.ItemWordExampleLayoutBinding

class DefinationAdapter(
    private var definationList: List<String>? = null,
    private var colorResId: Int? = null,
    private var desaturatedColorResId: Int? = null
) :
    RecyclerView.Adapter<DefinationAdapter.DefinationViewHolder>() {
    fun setDefinations(
        definationList: List<String>?,
        colorResId: Int?,
        desaturatedColorResId: Int?
    ) {
        this.definationList = definationList
        this.colorResId = colorResId;
        this.desaturatedColorResId = desaturatedColorResId;
        notifyDataSetChanged()
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

    override fun getItemCount(): Int = definationList?.size ?: 0

    override fun onBindViewHolder(holder: DefinationViewHolder, position: Int) {
        holder.binding.colorResId = colorResId
        holder.binding.desaturatedColorResId = desaturatedColorResId
        holder.binding.srNo = position + 1
        holder.binding.defination = definationList?.get(position)
        holder.binding.executePendingBindings()
    }
}