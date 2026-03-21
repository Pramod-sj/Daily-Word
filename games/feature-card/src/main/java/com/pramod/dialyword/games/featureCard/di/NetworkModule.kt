package com.pramod.dialyword.games.featureCard.di

import com.pramod.dailyword.games.results.GameEndpointProvider
import com.pramod.dialyword.games.featureCard.GamesApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(value = [SingletonComponent::class])
internal object NetworkModule {

    @Singleton
    @Provides
    fun provideGamesApiService(
        endpointProvider: GameEndpointProvider
    ): GamesApiService {
        return Retrofit.Builder()
            .baseUrl(endpointProvider.getBaseUrl())
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(GamesApiService::class.java)
    }

}
