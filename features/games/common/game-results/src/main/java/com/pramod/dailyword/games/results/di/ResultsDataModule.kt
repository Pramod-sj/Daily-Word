package com.pramod.dailyword.games.results.di


import com.pramod.dailyword.games.results.GameResultRepository
import com.pramod.dailyword.games.results.GameResultRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class ResultsDataModule {

    @Binds
    @Singleton
    abstract fun bindGameResultRepository(impl: GameResultRepositoryImpl): GameResultRepository
}
