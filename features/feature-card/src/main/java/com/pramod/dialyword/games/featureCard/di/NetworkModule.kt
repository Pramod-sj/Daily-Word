package com.pramod.dialyword.games.featureCard.di

import com.pramod.dailyword.network.di.NetworkCoreInstance
import com.pramod.dialyword.games.featureCard.GamesApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(value = [SingletonComponent::class])
internal object NetworkModule {

    @Singleton
    @Provides
    fun provideGamesApiService(
        @NetworkCoreInstance retrofit: Retrofit
    ): GamesApiService {
        return retrofit.create(GamesApiService::class.java)
    }

}
