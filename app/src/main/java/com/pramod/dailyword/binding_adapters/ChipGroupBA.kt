package com.pramod.dailyword.binding_adapters

import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ItemChipLayoutBinding
import com.pramod.dailyword.exts.resolveAttrToColor
import com.pramod.dailyword.exts.resolveAttrToDrawable
import com.pramod.dailyword.util.CommonUtils

object ChipGroupBA {
    @JvmStatic
    @BindingAdapter(
        value = ["app:chipEntries", "app:chipColor", "app:chipShowViewMoreButton", "app:onChipViewMoreClick"],
        requireAll = false
    )
    fun addChips(
        chipGroup: ChipGroup,
        chipTextList: List<String>?,
        chipColor: Int?,
        chipShowViewMoreButton: Boolean = false,
        onChipViewMoreClick: OnChipViewMoreClickListener?
    ) {
        chipTextList?.let {
            Log.i("CHIP TEXT", Gson().toJson(it))
            chipGroup.removeAllViews()
            for (chipText in it) {
                val binding: ItemChipLayoutBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(chipGroup.context),
                    R.layout.item_chip_layout,
                    chipGroup,
                    false
                )
                binding.chip.text = chipText
                chipColor?.let {
                    binding.chip.setTextColor(chipColor)
                    val color = ColorUtils.setAlphaComponent(chipColor, 20)
                    binding.chip.chipBackgroundColor = ColorStateList.valueOf(color)
                }
                chipGroup.addView(binding.root)
            }
            if (chipShowViewMoreButton) {
                val binding: ItemChipLayoutBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(chipGroup.context),
                    R.layout.item_chip_layout,
                    chipGroup,
                    false
                )
                binding.chip.text = "View More"
                binding.chip.setTextColor(
                    ContextCompat.getColor(
                        chipGroup.context,
                        R.color.colorPrimary
                    )
                )
                binding.chip.chipBackgroundColor =
                    ColorStateList.valueOf(chipGroup.context.resolveAttrToColor(android.R.attr.colorBackground))
                binding.chip.setOnClickListener { view ->
                    onChipViewMoreClick?.onViewMoreClick(view)
                }
                chipGroup.addView(
                    binding.chip
                )
            }
        }
    }
}

interface OnChipViewMoreClickListener {
    fun onViewMoreClick(v: View)
}