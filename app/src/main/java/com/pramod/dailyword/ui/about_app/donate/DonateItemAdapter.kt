package com.pramod.dailyword.ui.about_app.donate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ItemDonateLayoutBinding

class DonateItemAdapter(val itemClickCallback: ((Int, DonateItem) -> Unit)? = null) :
    ListAdapter<DonateItem, DonateItemAdapter.DonateItemViewHolder>(
        DiffCallback()
    ) {

    class DonateItemViewHolder(val binding: ItemDonateLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonateItemViewHolder {
        val itemDonateBinding =
            DataBindingUtil.inflate<ItemDonateLayoutBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_donate_layout, parent, false
            )
        return DonateItemViewHolder(itemDonateBinding)
    }

    override fun onBindViewHolder(holder: DonateItemViewHolder, position: Int) {
        holder.binding.donateItem = getItem(position)
        itemClickCallback?.let { callback ->
            holder.binding.root.setOnClickListener {
                callback.invoke(position, getItem(position))
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<DonateItem>() {
        override fun areItemsTheSame(oldItem: DonateItem, newItem: DonateItem): Boolean =
            oldItem.title == newItem.title

        override fun areContentsTheSame(oldItem: DonateItem, newItem: DonateItem): Boolean =
            oldItem == newItem

    }

}
