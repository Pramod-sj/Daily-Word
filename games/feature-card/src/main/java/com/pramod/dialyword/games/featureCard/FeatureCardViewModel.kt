package com.pramod.dialyword.games.featureCard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Defines the 3 states our UI can be in
sealed interface FeatureUiState {
    object Loading : FeatureUiState
    data class Success(val cards: List<FeatureCard>) : FeatureUiState
    data class Error(val message: String) : FeatureUiState
}


@HiltViewModel
internal class FeatureCardViewModel @Inject constructor(
    private val repository: FeatureCardRepository,
    private val dismissalRepository: DismissalRepository
) : ViewModel() {
    companion object {
        private const val TAG = "FeatureCardViewModel"
    }

    private val _featureCardsState = MutableStateFlow<FeatureUiState>(FeatureUiState.Loading)
    val featureCardsState: StateFlow<FeatureUiState>
        get() = combine(
            _featureCardsState,
            dismissalRepository.dismissedIds
        ) { state, dismissedIds ->
            when (state) {
                is FeatureUiState.Success -> {
                    FeatureUiState.Success(state.cards.filterNot { it.id in dismissedIds })
                }
                // If the API is still Loading or hit an Error, just pass that state through
                else -> state
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = FeatureUiState.Loading
        )


    init {
        viewModelScope.launch {
            repository.fetchLiveFeatures()
        }
    }

    fun getFeatureCard(screenName: String) {
        viewModelScope.launch {
            Log.i(TAG, "getFeatureCard: screenName:$screenName")
            repository.getFeatureCard(screenName)
                .collect {
                    Log.i(TAG, "getFeatureCard: card:$it")
                    _featureCardsState.value = it
                }
        }
    }

    fun onCardDismissed(card: FeatureCard) {
        viewModelScope.launch {
            dismissalRepository.dismiss(card)
        }
    }
}