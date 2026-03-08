package com.pramod.dialyword.games.core

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatureRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    //private val apiService: GamesApiService
) {
    private val _featuresState = MutableStateFlow<FeatureUiState>(FeatureUiState.Loading)
    val featuresState: StateFlow<FeatureUiState> = _featuresState.asStateFlow()

    suspend fun fetchLiveFeatures() {
        _featuresState.value = FeatureUiState.Loading
        try {
            val response = context.assets
                .open("feature_card.json")
                .bufferedReader()
                .use { it.readText() }
                .let {
                    Gson().fromJson(it, FeatureCardResponse::class.java)
                }
            //apiService.getLiveFeatures()

            _featuresState.value = FeatureUiState.Success(response.featureCards)

            /*
                        if (response.isSuccessful && response.body() != null) {
                            response.body()?.featureCards?.let {
                                _featuresState.value = FeatureUiState.Success(it)
                            } ?: run {
                                _featuresState.value = FeatureUiState.Error("Null body")
                            }
                        } else {
                            _featuresState.value = FeatureUiState.Error("API Error: ${response.code()}")
                        }
            */
        } catch (e: Exception) {
            _featuresState.value = FeatureUiState.Error(e.message ?: "Network Error")
        }
    }
}