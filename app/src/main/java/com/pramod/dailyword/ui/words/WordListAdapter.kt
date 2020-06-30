package com.pramod.dailyword.ui.words

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ItemNetworkStateLayoutBinding
import com.pramod.dailyword.databinding.ItemWordListLayoutBinding
import com.pramod.dailyword.db.model.NetworkState
import com.pramod.dailyword.db.model.NetworkState.Companion.LOADED
import com.pramod.dailyword.db.model.Status
import com.pramod.dailyword.db.model.WordOfTheDay
import java.lang.IllegalArgumentException


class WordListAdapter(
    val itemClickCallback: ((pos: Int, word: WordOfTheDay) -> Unit)? = null,
    val retryCallback: () -> Unit
) :
    PagedListAdapter<WordOfTheDay, RecyclerView.ViewHolder>(diffCallback) {
    private var networkState: NetworkState? = null
    private var canStartActivity = false

    fun setCanStartActivity(canStart: Boolean) {
        canStartActivity = canStart
    }

    inner class WordViewHolder(private val binding: ItemWordListLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, word: WordOfTheDay?) {
            binding.wordOfTheDay = word
            binding.itemWordListCardView.setOnClickListener {
                if (canStartActivity) {
                    canStartActivity = false;
                    itemClickCallback?.invoke(position, word!!)
                }
            }
            binding.executePendingBindings()
        }
    }

    class NetworkStateViewHolder(
        private val binding: ItemNetworkStateLayoutBinding,
        private val retryCallback: () -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.networkStateBtnRetry.setOnClickListener {
                retryCallback()
            }
        }

        fun bindTo(networkState: NetworkState?) {
            Log.i("Network State", Gson().toJson(networkState))
            binding.networkStateProgress.visibility =
                if (networkState?.status == Status.RUNNING) View.VISIBLE else View.INVISIBLE
            binding.networkStateErrorLayout.visibility =
                if (networkState?.status == Status.FAILED) View.VISIBLE else View.INVISIBLE
            binding.networkStateBtnRetry.visibility =
                if (networkState?.status == Status.FAILED) View.VISIBLE else View.INVISIBLE
            binding.networkStateTxtViewError.visibility =
                if (networkState?.msg != null) View.VISIBLE else View.INVISIBLE
            binding.networkStateTxtViewError.text =
                networkState?.msg
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_word_list_layout -> WordViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_word_list_layout,
                    parent,
                    false
                )
            )
            R.layout.item_network_state_layout -> NetworkStateViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_network_state_layout,
                    parent,
                    false
                ), retryCallback
            )
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.item_network_state_layout ->
                (holder as NetworkStateViewHolder).bindTo(networkState)
            R.layout.item_word_list_layout ->
                (holder as WordViewHolder).bind(position, getItem(position))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            R.layout.item_network_state_layout
        } else {
            R.layout.item_word_list_layout
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + (if (hasExtraRow()) 1 else 0)
    }

    private fun hasExtraRow() = networkState != null && networkState != LOADED

    fun setNetworkState(networkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = networkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow()) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != networkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    companion object {
        private val diffCallback =
            object : DiffUtil.ItemCallback<WordOfTheDay>() {
                override fun areItemsTheSame(
                    oldItem: WordOfTheDay,
                    newItem: WordOfTheDay
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: WordOfTheDay,
                    newItem: WordOfTheDay
                ): Boolean {
                    return oldItem == newItem
                }

                override fun getChangePayload(oldItem: WordOfTheDay, newItem: WordOfTheDay): Any? {
                    val bundle = Bundle()
                    if (oldItem.date != newItem.date) {
                        bundle.putString("DATE", newItem.date)
                    }
                    if (oldItem.word != newItem.word) {
                        bundle.putString("WORD", newItem.word)
                    }
                    if (Gson().toJson(oldItem.meanings) != Gson().toJson(newItem.meanings)) {
                        val array = arrayListOf<String>()
                        newItem.meanings?.let {
                            array.addAll(it.toMutableList())
                        }
                        bundle.putStringArrayList(
                            "DEFINATION",
                            array
                        )
                    }
                    return bundle
                }

            }
    }


}