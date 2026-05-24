package com.pramod.dailyword.games.results.di

import android.content.Context
import androidx.room.Room
import com.pramod.dailyword.games.results.GameResultDao
import com.pramod.dailyword.games.results.GameResultDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ResultsModule {

    @Singleton
    @Provides
    fun provideGameResultDatabase(@ApplicationContext context: Context): GameResultDatabase {
        return Room.databaseBuilder(
            context,
            GameResultDatabase::class.java,
            "game_results_db"
        ).build()
    }

    @Singleton
    @Provides
    fun provideGameResultDao(database: GameResultDatabase): GameResultDao {
        return database.gameResultDao()
    }
}