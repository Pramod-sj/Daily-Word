package com.pramod.dailyword.ui.word_details

import androidx.recyclerview.widget.DiffUtil
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.ui.donate.DonateItemAdapter

object StringDiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
}
