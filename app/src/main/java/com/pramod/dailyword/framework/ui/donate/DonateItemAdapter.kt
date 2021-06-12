package com.pramod.dailyword.framework.ui.donate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pramod.dailyword.Constants
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ItemDonateLayoutBinding
import com.pramod.dailyword.framework.util.CommonUtils

class DonateItemAdapter(val itemClickCallback: ((Int, DonateItem) -> Unit)? = null) :
    ListAdapter<DonateItem, DonateItemAdapter.DonateItemViewHolder>(
        DiffCallback()
    ) {

    inner class DonateItemViewHolder(val binding: ItemDonateLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                itemClickCallback?.invoke(bindingAdapterPosition, getItem(bindingAdapterPosition))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonateItemViewHolder {
        val itemDonateBinding =
            DataBindingUtil.inflate<ItemDonateLayoutBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_donate_layout, parent, false
            )
        return DonateItemViewHolder(itemDonateBinding)
    }

    override fun onBindViewHolder(holder: DonateItemViewHolder, position: Int) {
        val donateItem = getItem(position)
        holder.binding.donateItem = donateItem
        val color = CommonUtils.getColor(holder.binding.root.context, donateItem.color)
        holder.binding.color = color
        val alphaAppliedColor =
            CommonUtils.changeAlpha(
                holder.binding.root.context,
                donateItem.color,
                Constants.COLOR_ALPHA_DONATE_ITEM
            )
        holder.binding.alphaColor = alphaAppliedColor
        holder.binding.strokeColor =
            CommonUtils.changeAlpha(holder.binding.root.context, donateItem.color, 90)

        holder.binding.executePendingBindings()
    }

    class DiffCallback : DiffUtil.ItemCallback<DonateItem>() {
        override fun areItemsTheSame(oldItem: DonateItem, newItem: DonateItem): Boolean =
            oldItem.title == newItem.title

        override fun areContentsTheSame(oldItem: DonateItem, newItem: DonateItem): Boolean =
            oldItem == newItem

        override fun getChangePayload(oldItem: DonateItem, newItem: DonateItem): Any? {
            return bundleOf(
                "amount" to newItem.amount,
                "color" to newItem.color,
                "isAlreadyDonated" to newItem.isAlreadyDonated,
            )
        }

    }

}
