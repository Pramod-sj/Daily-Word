package com.pramod.dailyword.framework.ui.words

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pramod.dailyword.R
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.databinding.ItemAdLayoutBinding
import com.pramod.dailyword.databinding.ItemEmptyLayoutBinding
import com.pramod.dailyword.databinding.ItemWordListLayoutBinding
import com.pramod.dailyword.framework.helper.ads.AdController
import com.pramod.dailyword.framework.ui.common.word.WordComparator
import com.pramod.dailyword.framework.ui.common.word.WordListUiModel


class WordsAdapter(
    private val itemClickCallback: ((pos: Int, word: Word) -> Unit)? = null,
    private val bookmarkCallback: ((pos: Int, word: Word) -> Unit)? = null,
    private val hideBadges: Boolean = false,
    private val adController: AdController
) : PagingDataAdapter<WordListUiModel, RecyclerView.ViewHolder>(WordComparator) {

    private var canStartActivity = true

    companion object {
        private const val ITEM_TYPE_WORD = 0
        private const val ITEM_TYPE_AD = 1
    }

    fun setCanStartActivity(canStart: Boolean) {
        canStartActivity = canStart
    }

    inner class WordViewHolder(private val binding: ItemWordListLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.itemWordListCardView.setOnClickListener {
                if (canStartActivity) {
                    canStartActivity = false
                    itemClickCallback?.invoke(
                        bindingAdapterPosition,
                        (getItem(bindingAdapterPosition) as WordListUiModel.WordItem).word
                    )
                }
            }
            binding.imgBtnBookmarkWordList.setOnClickListener {
                bookmarkCallback?.invoke(
                    bindingAdapterPosition,
                    (getItem(bindingAdapterPosition) as WordListUiModel.WordItem).word
                )
            }

        }

        fun bind(wordItem: WordListUiModel.WordItem) {
            binding.itemWordListCardView.transitionName = wordItem.word.date
            binding.wordItem = wordItem
            binding.hideBadge = hideBadges
            binding.executePendingBindings()
        }
    }

    inner class AdViewHolder(
        private val binding: ItemAdLayoutBinding,
        private val adController: AdController
    ) : RecyclerView.ViewHolder(binding.root) {
        fun loadAd() {
            adController.loadBanner(binding.adContainer)
        }

        fun clearAd() {
            adController.hideBanner(binding.adContainer)
        }

    }

    inner class EmptyViewHolder(binding: ItemEmptyLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is WordListUiModel.AdItem -> ITEM_TYPE_AD
            is WordListUiModel.WordItem -> ITEM_TYPE_WORD
            else -> -1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_TYPE_WORD -> WordViewHolder(
                DataBindingUtil.inflate(
                    inflater,
                    R.layout.item_word_list_layout,
                    parent,
                    false
                )
            )

            ITEM_TYPE_AD -> AdViewHolder(
                binding = DataBindingUtil.inflate(
                    inflater,
                    R.layout.item_ad_layout,
                    parent,
                    false
                ),
                adController = adController
            )

            else -> EmptyViewHolder(ItemEmptyLayoutBinding.inflate(inflater, parent, false))
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is WordViewHolder) {
            val wordItem = getItem(position) as WordListUiModel.WordItem
            holder.bind(wordItem)
        } else if (holder is AdViewHolder) {
            holder.loadAd()
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is AdViewHolder) {
            holder.clearAd()
        }
    }


}