package com.pramod.dialyword.games.featureCard.di

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
    fun provideGamesApiService(): GamesApiService {
        return Retrofit.Builder()
            .baseUrl("https://dailyword.xyz/ci/index.php/")
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(GamesApiService::class.java)
    }

}
