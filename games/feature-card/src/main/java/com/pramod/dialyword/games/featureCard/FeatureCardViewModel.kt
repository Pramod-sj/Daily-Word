package com.pramod.dialyword.games.featureCard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val repository: FeatureCardRepository
) : ViewModel() {

    val featuresState = repository.featuresState

    init {
        loadFeatures()
    }

    fun loadFeatures() {
        viewModelScope.launch {
            repository.fetchLiveFeatures()
        }
    }
}
