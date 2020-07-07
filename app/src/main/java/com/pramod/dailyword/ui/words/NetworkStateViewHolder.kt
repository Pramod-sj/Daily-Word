package com.pramod.dailyword.ui.words

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.pramod.dailyword.databinding.ItemNetworkStateLayoutBinding
import com.pramod.dailyword.db.model.NetworkState
import com.pramod.dailyword.db.model.Status

class NetworkStateViewHolder(
    private val binding: ItemNetworkStateLayoutBinding,
    private val retryCallback: () -> Unit
) :
    RecyclerView.ViewHolder(binding.root) {
    init {
        binding.networkStateBtnRetry.setOnClickListener {
            retryCallback.invoke()
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
