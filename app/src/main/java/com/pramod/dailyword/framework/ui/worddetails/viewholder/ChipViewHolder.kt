package com.pramod.dailyword.framework.ui.worddetails.viewholder

import android.view.View
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ItemWdChipsLayoutBinding
import com.pramod.dailyword.framework.ui.common.BaseViewHolder
import com.pramod.dailyword.framework.ui.common.bindingadapter.ChipGroupBA.addChips
import com.pramod.dailyword.framework.ui.common.bindingadapter.OnChipClickListener
import com.pramod.dailyword.framework.ui.common.bindingadapter.OnChipViewMoreClickListener
import com.pramod.dailyword.framework.ui.worddetails.WordDetailAdapterItemClickListener
import com.pramod.dailyword.framework.ui.worddetails.uimodel.WordDetailUiModel

/**
 * Created by Pramod on 30,December,2021
 */
class ChipViewHolder(
    private val binding: ItemWdChipsLayoutBinding,
    private val listener: WordDetailAdapterItemClickListener
) : BaseViewHolder<WordDetailUiModel>(binding) {

    override fun bind(data: WordDetailUiModel) {
        data as WordDetailUiModel.ChipsCard
        binding.tvTitleText.text = data.title
        addChips(
            chipGroup = binding.chipGroupSynonyms,
            chipTextList = data.chipTexts.take(6),
            chipColor = data.wordColor,
            chipShowViewMoreButton = data.chipTexts.size > 6,
            onChipViewMoreClick = object : OnChipViewMoreClickListener {
                override fun onViewMoreClick(v: View) {
                    listener.onChipsCardViewMoreClick(data.title, data.chipTexts)
                }
            },
            onChipClickListener = object : OnChipClickListener {
                override fun onChipClick(text: String) {
                    listener.onChipsCardChipClick(
                        String.format(
                            "%s%s", context.resources.getString(R.string.google_search_url), text
                        )
                    )
                }
            }
        )
        binding.ivInfo.setOnClickListener {
            listener.onChipsCardInfoClick(data.title, data.infoHint)
        }
    }
}