package com.pramod.games.crossword.di

import com.pramod.games.crossword.network.CrossWordApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(value = [SingletonComponent::class])
internal object NetworkModule {

    @Singleton
    @Provides
    fun provideCrossWordApiService(): CrossWordApiService {
        return Retrofit.Builder()
            .baseUrl("https://dailyword.xyz/ci/index.php/")
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(CrossWordApiService::class.java)
    }

}
