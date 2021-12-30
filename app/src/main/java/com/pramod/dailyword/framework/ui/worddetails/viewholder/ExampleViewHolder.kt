package com.pramod.dailyword.framework.ui.worddetails.viewholder

import android.content.res.ColorStateList
import androidx.core.graphics.ColorUtils
import com.pramod.dailyword.databinding.ItemWdExampleLayoutBinding
import com.pramod.dailyword.databinding.ItemWordExampleLayoutBinding
import com.pramod.dailyword.framework.ui.common.BaseViewHolder
import com.pramod.dailyword.framework.ui.common.SimpleListAdapter
import com.pramod.dailyword.framework.ui.worddetails.StringComparator
import com.pramod.dailyword.framework.ui.worddetails.uimodel.WordDetailUiModel

/**
 * Created by Pramod on 29,December,2021
 */
class ExampleViewHolder(private val binding: ItemWdExampleLayoutBinding) :
    BaseViewHolder<WordDetailUiModel>(
        binding
    ) {

    private var wordColor: Int? = null

    private val exampleAdapter by lazy {
        SimpleListAdapter(
            inflate = ItemWordExampleLayoutBinding::inflate,
            itemComparator = StringComparator,
            onBind = { pos, rowBinding, data ->
                rowBinding.tvExampleText.text = data
                with(rowBinding.tvSrNo) {
                    text = (pos + 1).toString()
                    backgroundTintList = wordColor?.let {
                        ColorStateList.valueOf(ColorUtils.setAlphaComponent(it, 30))
                    }
                    wordColor?.let { setTextColor(ColorStateList.valueOf(it)) }
                }
            },
        )
    }

    init {
        binding.rvExamples.adapter = exampleAdapter
    }

    override fun bind(data: WordDetailUiModel) {
        data as WordDetailUiModel.ExampleCard
        wordColor = data.wordColor
        binding.tvHowToUseLabel.text = String.format("How to use %s", data.word)
        exampleAdapter.submitList(data.examples)
    }
}