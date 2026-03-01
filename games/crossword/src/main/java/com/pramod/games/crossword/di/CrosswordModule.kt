package com.pramod.games.crossword.di

import android.content.Context
import com.google.gson.Gson
import com.pramod.games.crossword.network.CrosswordRepository
import com.pramod.games.crossword.network.DummyRepositoryImpl
import com.pramod.games.crossword.ui.boardGenerator.CrosswordMapGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(value = [SingletonComponent::class])
internal class CrosswordModule {
    @Singleton
    @Provides
    fun provideCrosswordRepo(
        @ApplicationContext context: Context,
    ): CrosswordRepository = DummyRepositoryImpl(context, Gson())

    @Singleton
    @Provides
    fun provideCrosswordMapGenerator(): CrosswordMapGenerator = CrosswordMapGenerator()
}
