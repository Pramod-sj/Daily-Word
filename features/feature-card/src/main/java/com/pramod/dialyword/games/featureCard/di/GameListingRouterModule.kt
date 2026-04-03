package com.pramod.dialyword.games.featureCard.di

import com.pramod.dialyword.games.featureCard.router.GameListingRouter
import com.pramod.dialyword.router.FeatureRouter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
 abstract class GameListingRouterModule {

    @Binds
    @IntoSet
    abstract fun bindGameListingRouter(
        gameListingRouter: GameListingRouter
    ): FeatureRouter

}