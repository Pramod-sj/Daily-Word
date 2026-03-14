package com.pramod.dialyword.games.featureCard

import com.pramod.dailyword.games.results.GameResultRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatureCardRepositoryImpl @Inject constructor(
    private val apiService: GamesApiService,
    private val gameResultRepository: GameResultRepository
) : FeatureCardRepository {

    private val _apiState = MutableStateFlow<FeatureUiState>(FeatureUiState.Loading)

    override val featuresState: StateFlow<FeatureUiState> = combine(
        _apiState,
        gameResultRepository.getAllResults()
    ) { apiState, allLocalResults ->

        when (apiState) {
            is FeatureUiState.Success -> {
                // Enrich the cards with the latest Room data
                val enrichedCards = apiState.cards.map { card ->
                    val localResult = allLocalResults.find { it.puzzleId == card.gameId }
                    card.copy(result = localResult)
                }
                FeatureUiState.Success(enrichedCards)
            }
            // If the API is still Loading or hit an Error, just pass that state through
            else -> apiState
        }
    }.stateIn(
        scope = CoroutineScope(context = Dispatchers.Default),
        started = SharingStarted.Lazily,
        initialValue = FeatureUiState.Loading
    )

    override suspend fun fetchLiveFeatures() {
        try {
            val response = apiService.getLiveFeatures()
            if (response.isSuccessful && response.body() != null) {
                val rawCards = response.body()?.featureCards ?: emptyList()
                _apiState.value = FeatureUiState.Success(rawCards)
            } else {
                _apiState.value = FeatureUiState.Error("API Error: ${response.code()}")
            }
        } catch (e: Exception) {
            _apiState.value = FeatureUiState.Error(e.message ?: "Network Error")
        }
    }
}