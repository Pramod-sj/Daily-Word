package com.pramod.dialyword.games.featureCard.paging

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages and provides observable states for data loading operations, specifically handling
 * refresh and append (pagination) states.
 *
 * This class tracks whether the system is currently loading, idle, or in an error state
 * for different [LoadType] operations, providing both raw [LoadState] flows
 *
 */
class LoadStateHolder() {

    private val _refreshState = MutableStateFlow<LoadState>(LoadState.Idle)
    val refreshState: StateFlow<LoadState> = _refreshState.asStateFlow()

    private val _appendState = MutableStateFlow<LoadState>(LoadState.Idle)
    val appendState: StateFlow<LoadState> = _appendState.asStateFlow()

    fun setRefreshState(state: LoadState) {
        _refreshState.value = state
    }

    fun setAppendState(state: LoadState) {
        _appendState.value = state
    }

    fun setState(loadType: LoadType, state: LoadState) {
        when (loadType) {
            LoadType.REFRESH -> setRefreshState(state)
            LoadType.APPEND -> setAppendState(state)
        }
    }

    fun resetAppendState() {
        _appendState.value = LoadState.Idle
    }

    fun hasRefreshError(): Boolean = _refreshState.value is LoadState.Error

    fun hasAppendError(): Boolean = _appendState.value is LoadState.Error

    fun isAppendingOrError(): Boolean =
        _appendState.value is LoadState.Loading || _appendState.value is LoadState.Error

}