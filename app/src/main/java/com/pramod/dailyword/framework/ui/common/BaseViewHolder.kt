package com.pramod.dailyword.framework.ui.common

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * Created by Pramod on 29,December,2021
 */
abstract class BaseViewHolder<Data : Any>(binding: ViewBinding) :
    RecyclerView.ViewHolder(binding.root) {

    val context: Context = binding.root.context

    abstract fun bind(data: Data)

    open fun onAttachedToWindow() {}

    open fun onDetachedFromWindow() {}
}