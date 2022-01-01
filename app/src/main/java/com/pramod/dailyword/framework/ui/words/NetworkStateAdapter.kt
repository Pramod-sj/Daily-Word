package com.pramod.dailyword.framework.ui.words

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.databinding.DataBindingUtil
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ItemNetworkStateLayoutBinding
import timber.log.Timber

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
            binding.networkStateProgress.isInvisible = loadState != LoadState.Loading
            binding.networkStateErrorLayout.isInvisible = loadState !is LoadState.Error
            binding.networkStateBtnRetry.isInvisible = loadState !is LoadState.Error
            binding.networkStateTxtViewError.isInvisible = loadState !is LoadState.Error
            if (loadState is LoadState.Error) {
                binding.networkStateTxtViewError.text = loadState.error.message
            }
        }

    }


    override fun onBindViewHolder(holder: NetworkStateViewHolder, loadState: LoadState) {
        holder.bindTo(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): NetworkStateViewHolder {
        Timber.i( "onCreateViewHolder: ")
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