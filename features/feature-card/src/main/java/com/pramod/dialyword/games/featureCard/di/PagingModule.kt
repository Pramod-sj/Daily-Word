package com.pramod.dialyword.games.featureCard.di

import com.pramod.dialyword.games.featureCard.FeatureCard
import com.pramod.dialyword.games.featureCard.GameListingPagingDataSource
import com.pramod.dialyword.games.featureCard.GamesApiService
import com.pramod.dialyword.games.featureCard.paging.PagingDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(value = [SingletonComponent::class])
internal class PagingModule {

    @Provides
    fun provideGameListingPagingDataSource(
        gamesApiService: GamesApiService
    ): PagingDataSource<FeatureCard> {
        return GameListingPagingDataSource(gamesApiService)
    }

}