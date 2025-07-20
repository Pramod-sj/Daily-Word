package com.pramod.dailyword.di

import com.pramod.dailyword.framework.prefmanagers.EdgeToEdgeApplicator
import com.pramod.dailyword.framework.prefmanagers.EdgeToEdgeEnabler
import com.pramod.dailyword.framework.prefmanagers.EdgeToEdgePrefManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(value = [SingletonComponent::class])
interface Edge2EdgeModule {

    @Singleton
    @Binds
    fun bindEdgeToEdgeEnabler(edgeToEdgePrefManagerImpl: EdgeToEdgePrefManagerImpl): EdgeToEdgeEnabler

    @Singleton
    @Binds
    fun bindEdgeToEdgeApplicator(edgeToEdgePrefManagerImpl: EdgeToEdgePrefManagerImpl): EdgeToEdgeApplicator

}