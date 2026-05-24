package com.pramod.dialyword.games.featureCard

import kotlinx.coroutines.flow.StateFlow

interface FeatureCardRepository {

    val featuresState: StateFlow<FeatureUiState>

    suspend fun fetchLiveFeatures()

    suspend fun getFeatureCard(screenName: String): StateFlow<FeatureUiState>

}