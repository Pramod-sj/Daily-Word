package com.pramod.dailyword.framework.ui.worddetails

import androidx.recyclerview.widget.DiffUtil

object StringComparator : DiffUtil.ItemCallback<String>() {

    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: String, newItem: String): Any {
        return newItem
    }
}
