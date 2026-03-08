package com.pramod.dialyword.games.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

// Defines the 3 states our UI can be in
sealed interface FeatureUiState {
    object Loading : FeatureUiState
    data class Success(val cards: List<FeatureCard>) : FeatureUiState
    data class Error(val message: String) : FeatureUiState
}

@HiltViewModel
class FeatureCardViewModel @Inject constructor(
    private val repository: FeatureRepository
) : ViewModel() {

    private val _cards = MutableStateFlow<List<FeatureCard>>(emptyList())
    val cards: StateFlow<List<FeatureCard>> = _cards

    init {
        repository.featuresState.onEach {
            when (it) {
                is FeatureUiState.Error -> Unit
                FeatureUiState.Loading -> Unit
                is FeatureUiState.Success -> {
                    _cards.value = it.cards
                }
            }

        }.launchIn(viewModelScope)
        // Fetch the cards immediately when the ViewModel is created
        loadFeatures()
    }

    fun loadFeatures() {
        viewModelScope.launch {
            repository.fetchLiveFeatures()
        }
    }
}