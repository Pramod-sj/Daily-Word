package com.pramod.dailyword.framework.ui.bookmarks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pramod.dailyword.R
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.databinding.ItemBookmarkedWordLayoutBinding


class BookmarkedWordsAdapter(
        val itemClickCallback: ((pos: Int, word: Word) -> Unit)? = null,
        val deleteBookmarkCallback: (word: Word) -> Unit,
        private val hideBadges: Boolean = false
) : PagingDataAdapter<Word, BookmarkedWordsAdapter.BookmarkedWordViewHolder>(WordItemComparator) {
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
            binding.hideBadge = hideBadges
            binding.root.setOnClickListener {
                if (canStartActivity) {
                    canStartActivity = false;
                    itemClickCallback?.invoke(bindingAdapterPosition, word)
                }
            }
            binding.imgBtnBookmark.setOnClickListener {
                deleteBookmarkCallback.invoke(getItem(bindingAdapterPosition)!!)
            }
            binding.executePendingBindings()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkedWordViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return BookmarkedWordViewHolder(
                DataBindingUtil.inflate(
                        inflater,
                        R.layout.item_bookmarked_word_layout,
                        parent,
                        false
                )
        )
    }


    override fun onBindViewHolder(holder: BookmarkedWordViewHolder, position: Int) {
        val wordItem = getItem(position)
                ?: throw IllegalStateException("word cannot be empty for: position : $position")
        holder.bind(wordItem)
    }


}