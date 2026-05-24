package com.pramod.dailyword.di

import com.pramod.dailyword.framework.router.WordFeatureRouter
import com.pramod.dialyword.router.FeatureRouter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class RouterModule {

    @Binds
    @IntoSet
    abstract fun bindWordFeatureRouter(wordFeatureRouter: WordFeatureRouter): FeatureRouter

}