package com.pramod.dailyword.ui.words

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.ItemNetworkStateLayoutBinding
import com.pramod.dailyword.db.model.NetworkState

class NetworkStateAdapter(
    private val networkState: NetworkState,
    private val retryCallback: () -> Unit
) :
    RecyclerView.Adapter<NetworkStateViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NetworkStateViewHolder {
        val binding: ItemNetworkStateLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_network_state_layout,
            parent,
            false
        )
        return NetworkStateViewHolder(binding, retryCallback)
    }


    override fun onBindViewHolder(holder: NetworkStateViewHolder, position: Int) {
        holder.bindTo(networkState)
    }


    override fun getItemCount(): Int = 1
}