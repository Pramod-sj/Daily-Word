package com.pramod.games.crossword.di

import com.pramod.games.crossword.network.CrosswordRepository
import com.pramod.games.crossword.network.CrosswordRepositoryImpl
import com.pramod.games.crossword.ui.boardGenerator.CrosswordMapGenerator
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class CrosswordModule {

    @Singleton
    @Provides
    fun provideCrosswordMapGenerator(): CrosswordMapGenerator = CrosswordMapGenerator()
}


@Module
@InstallIn(SingletonComponent::class)
internal abstract class CrosswordDataModule {

    @Binds
    abstract fun bindCrosswordRepo(crosswordRepositoryImpl: CrosswordRepositoryImpl): CrosswordRepository

}
