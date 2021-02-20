package com.pramod.dailyword.framework.ui.common.bindingadapter

import android.content.res.ColorStateList
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ItemChipLayoutBinding

object ChipGroupBA {
    @JvmStatic
    @BindingAdapter(
        value = ["app:chipEntries", "app:chipColor", "app:chipShowViewMoreButton", "app:onChipViewMoreClick", "app:onChipClick"],
        requireAll = false
    )
    fun addChips(
        chipGroup: ChipGroup,
        chipTextList: List<String>?,
        chipColor: Int?,
        chipShowViewMoreButton: Boolean = false,
        onChipViewMoreClick: OnChipViewMoreClickListener?,
        onChipClickListener: OnChipClickListener?
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
                binding.chip.setOnClickListener { view ->
                    onChipClickListener?.onChipClick((view as Chip).text.toString())
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
                binding.chip.text = SpannableString("more").also { string ->
                    string.setSpan(UnderlineSpan(), 0, 4, SpannableString.SPAN_INCLUSIVE_INCLUSIVE)
                }
                binding.chip.textSize = 13f
                binding.chip.chipBackgroundColor =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            chipGroup.context,
                            android.R.color.transparent
                        )
                    )
                binding.chip.background = null
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

interface OnChipClickListener {
    fun onChipClick(text: String)
}

interface OnChipViewMoreClickListener {
    fun onViewMoreClick(v: View)
}