package com.pramod.dialyword.games.featureCard.paging

sealed class LoadState {

    object Idle : LoadState()

    object Loading : LoadState()

    data class Error(val throwable: Throwable) : LoadState()

}
