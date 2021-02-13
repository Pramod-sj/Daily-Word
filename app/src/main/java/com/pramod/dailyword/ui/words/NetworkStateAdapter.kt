package com.pramod.dailyword.ui.words

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ItemNetworkStateLayoutBinding

class NetworkStateAdapter(
    private val retryCallback: () -> Unit
) : LoadStateAdapter<NetworkStateAdapter.NetworkStateViewHolder>() {

    inner class NetworkStateViewHolder(
        private val binding: ItemNetworkStateLayoutBinding,
        private val retryCallback: () -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.networkStateBtnRetry.setOnClickListener {
                retryCallback.invoke()
            }
        }

        fun bindTo(loadState: LoadState) {
            Log.i("TAG", "bindTo: " + Gson().toJson(loadState))
            binding.networkStateProgress.isVisible = loadState == LoadState.Loading
            binding.networkStateErrorLayout.isVisible = loadState is LoadState.Error
            binding.networkStateBtnRetry.isVisible = loadState is LoadState.Error
            binding.networkStateTxtViewError.isVisible = loadState is LoadState.Error
            binding.networkStateTxtViewError.text =
                (loadState as LoadState.Error).error.message
        }

    }


    override fun onBindViewHolder(holder: NetworkStateViewHolder, loadState: LoadState) {
        holder.bindTo(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): NetworkStateViewHolder {
        Log.i(TAG, "onCreateViewHolder: ")
        val binding: ItemNetworkStateLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_network_state_layout,
            parent,
            false
        )
        return NetworkStateViewHolder(binding, retryCallback)
    }

    companion object {
        val TAG = NetworkStateAdapter::class.simpleName
    }

}