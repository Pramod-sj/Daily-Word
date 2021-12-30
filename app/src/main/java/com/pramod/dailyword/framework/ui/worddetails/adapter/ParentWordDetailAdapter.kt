package com.pramod.dailyword.framework.ui.worddetails.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.library.audioplayer.AudioPlayer
import com.pramod.dailyword.databinding.ItemWdChipsLayoutBinding
import com.pramod.dailyword.databinding.ItemWdExampleLayoutBinding
import com.pramod.dailyword.databinding.ItemWdWordBasicInfoLayoutBinding
import com.pramod.dailyword.framework.ui.common.BaseViewHolder
import com.pramod.dailyword.framework.ui.worddetails.AsyncWordDetailUiModelComparator
import com.pramod.dailyword.framework.ui.worddetails.WordDetailAdapterItemClickListener
import com.pramod.dailyword.framework.ui.worddetails.uimodel.WordDetailUiModel
import com.pramod.dailyword.framework.ui.worddetails.uimodel.WordDetailUiModelViewType
import com.pramod.dailyword.framework.ui.worddetails.viewholder.ChipViewHolder
import com.pramod.dailyword.framework.ui.worddetails.viewholder.ExampleViewHolder
import com.pramod.dailyword.framework.ui.worddetails.viewholder.WordTopCardViewHolder

/**
 * Created by Pramod on 29,December,2021
 */
class ParentWordDetailAdapter(
    private val audioPlayer: AudioPlayer,
    private val listener: WordDetailAdapterItemClickListener
) : ListAdapter<WordDetailUiModel, BaseViewHolder<WordDetailUiModel>>(
    AsyncWordDetailUiModelComparator
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<WordDetailUiModel> {
        val inflater = LayoutInflater.from(parent.context)
        return when (WordDetailUiModelViewType.fromType(viewType)) {
            WordDetailUiModelViewType.WORD_INFO_TOP_CARD -> {
                WordTopCardViewHolder(
                    binding = ItemWdWordBasicInfoLayoutBinding.inflate(
                        inflater, parent, false
                    ),
                    audioPlayer = audioPlayer,
                    listener = listener
                )
            }
            WordDetailUiModelViewType.EXAMPLE_CARD -> {
                ExampleViewHolder(
                    binding = ItemWdExampleLayoutBinding.inflate(inflater, parent, false)
                )
            }
            WordDetailUiModelViewType.CHIPS_CARD -> ChipViewHolder(
                binding = ItemWdChipsLayoutBinding.inflate(inflater, parent, false),
                listener = listener
            )
            WordDetailUiModelViewType.DID_YOU_KNOW -> TODO()
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<WordDetailUiModel>, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).viewType.type
    }

    override fun onViewAttachedToWindow(holder: BaseViewHolder<WordDetailUiModel>) {
        super.onViewAttachedToWindow(holder)
        holder.onAttachedToWindow()
    }

    override fun onViewDetachedFromWindow(holder: BaseViewHolder<WordDetailUiModel>) {
        super.onViewDetachedFromWindow(holder)
        holder.onDetachedFromWindow()
    }

}

