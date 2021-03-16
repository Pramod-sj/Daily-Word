package com.pramod.dailyword.framework.ui.bookmarks

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pramod.dailyword.R
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.databinding.ItemAdLayoutBinding
import com.pramod.dailyword.databinding.ItemBookmarkedWordLayoutBinding
import com.pramod.dailyword.framework.ui.common.word.WordComparator
import com.pramod.dailyword.framework.ui.common.word.WordListUiModel


class BookmarkedWordsAdapter(
    val itemClickCallback: ((pos: Int, word: Word) -> Unit)? = null,
    val deleteBookmarkCallback: (word: Word) -> Unit
) : PagingDataAdapter<WordListUiModel, RecyclerView.ViewHolder>(WordComparator) {
    private var canStartActivity = true

    companion object {
        private const val ITEM_TYPE_WORD = 0
        private const val ITEM_TYPE_AD = 1
    }

    fun setCanStartActivity(canStart: Boolean) {
        canStartActivity = canStart
    }

    inner class BookmarkedWordViewHolder(private val binding: ItemBookmarkedWordLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(word: Word) {
            binding.root.transitionName = word.date
            binding.word = word
            binding.root.setOnClickListener {
                if (canStartActivity) {
                    canStartActivity = false;
                    itemClickCallback?.invoke(bindingAdapterPosition, word)
                }
            }
            binding.imgBtnBookmark.setOnClickListener {
                deleteBookmarkCallback.invoke((getItem(bindingAdapterPosition) as WordListUiModel.WordItem).word)
            }
            binding.executePendingBindings()
        }
    }

    inner class AdViewHolder(binding: ItemAdLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

    }


    override fun getItemViewType(position: Int): Int {
        Log.i("TAG", "getItemViewType: " + getItem(position)?.javaClass?.simpleName)
        return when (getItem(position)) {
            is WordListUiModel.AdItem -> ITEM_TYPE_AD
            is WordListUiModel.WordItem -> ITEM_TYPE_WORD
            else -> -1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_TYPE_WORD -> BookmarkedWordViewHolder(
                DataBindingUtil.inflate(
                    inflater,
                    R.layout.item_bookmarked_word_layout,
                    parent,
                    false
                )
            )
            ITEM_TYPE_AD -> AdViewHolder(
                DataBindingUtil.inflate(
                    inflater,
                    R.layout.item_ad_layout,
                    parent,
                    false
                )
            )
            else -> throw ClassCastException("Unknown viewType $viewType $itemCount")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BookmarkedWordViewHolder) {
            val wordItem = getItem(position) as WordListUiModel.WordItem
            holder.bind(wordItem.word)
        }
    }


}