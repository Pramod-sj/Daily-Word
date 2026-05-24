package com.pramod.dialyword.games.featureCard.di

import com.pramod.dialyword.games.featureCard.FeatureCardRepository
import com.pramod.dialyword.games.featureCard.FeatureCardRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(value = [SingletonComponent::class])
internal interface FeatureCardDataModule {

    @Singleton
    @Binds
    fun bindFeatureCardRepository(
        featureCardRepositoryImpl: FeatureCardRepositoryImpl
    ): FeatureCardRepository

}
