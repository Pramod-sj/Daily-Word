package com.pramod.games.crossword.di

import com.pramod.dailyword.network.di.NetworkCoreInstance
import com.pramod.games.crossword.network.CrossWordApiService
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
    fun provideCrossWordApiService(
         @NetworkCoreInstance retrofit: Retrofit
    ): CrossWordApiService {
        return retrofit.create(CrossWordApiService::class.java)
    }

}
