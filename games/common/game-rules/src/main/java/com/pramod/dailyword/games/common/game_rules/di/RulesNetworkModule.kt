package com.pramod.dailyword.games.common.game_rules.di

import com.pramod.dailyword.games.common.game_rules.network.GameRulesApiService
import com.pramod.dailyword.network.di.NetworkCoreInstance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RulesNetworkModule {

    @Provides
    @Singleton
    fun provideGameRulesApiService(
        @NetworkCoreInstance retrofit: Retrofit // Hilt grabs your base Retrofit client from your Core network module
    ): GameRulesApiService {
        return retrofit.create(GameRulesApiService::class.java)
    }
}