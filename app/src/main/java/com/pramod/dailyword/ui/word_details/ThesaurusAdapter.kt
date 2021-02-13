package com.pramod.dailyword.ui.word_details

import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ItemChipLayoutBinding
import com.pramod.dailyword.util.CommonUtils

class ThesaurusAdapter(
    val showAll: Boolean = true,
    val itemClickCallback: ((String) -> Unit)? = null,
    val moreClickCallback: (() -> Unit)? = null,
    var moreTextColor: Int? = null
) : ListAdapter<String, RecyclerView.ViewHolder>(StringDiffCallback) {
    private val ITEM_TYPE_MORE_BUTTON = 1
    private val ITEM_TYPE_CHIP = 0

    fun setMoreTextColor(moreTextColor: Int) {
        this.moreTextColor = moreTextColor
        if (itemCount == 6) {
            notifyItemChanged(5)
        } else {
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(val binding: ItemChipLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.chip.setOnClickListener {
                itemClickCallback?.invoke(getItem(bindingAdapterPosition))
            }
        }
    }

    inner class MoreButtonViewHolder(private val frameLayout: FrameLayout) :
        RecyclerView.ViewHolder(frameLayout) {
        init {
            val textView: TextView = frameLayout.getChildAt(0) as TextView
            textView.setTextColor(
                moreTextColor ?: CommonUtils.resolveAttrToColor(
                    textView.context,
                    R.attr.colorPrimary
                )
            )
            textView.textSize = 13f
            textView.text = SpannableString("more").also {
                it.setSpan(UnderlineSpan(), 0, 4, SpannableString.SPAN_INCLUSIVE_INCLUSIVE)
            }
            textView.setOnClickListener {
                moreClickCallback?.invoke()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM_TYPE_CHIP) {
            val binding: ItemChipLayoutBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_chip_layout,
                parent,
                false
            )
            return ViewHolder(binding)
        } else {
            val frameLayout = FrameLayout(parent.context)
            frameLayout.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            val textView = TextView(parent.context)
            textView.setPadding(10, 0, 10, 0)
            textView.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).also {
                it.gravity = Gravity.CENTER
            }
            frameLayout.addView(textView)
            return MoreButtonViewHolder(frameLayout)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.binding.chip.text = getItem(position)
            holder.binding.executePendingBindings()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (!showAll) (if (position <= 2) ITEM_TYPE_CHIP else ITEM_TYPE_MORE_BUTTON) else ITEM_TYPE_CHIP
    }

    override fun getItemCount(): Int {
        return if (!showAll) if (super.getItemCount() >= 3 && super.getItemCount() != 0) 4 else super.getItemCount() else super.getItemCount()
    }

}