package com.pramod.games.crossword.di

import com.pramod.dialyword.router.FeatureRouter
import com.pramod.games.crossword.router.GamesFeatureRouter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CrosswordRouterModule {

    @Binds
    @IntoSet
    abstract fun bindGamesFeatureRouter(router: GamesFeatureRouter): FeatureRouter
}