package com.pramod.dailyword.di

import com.pramod.dailyword.business.data.network.abstraction.IPInfoNetworkDataSource
import com.pramod.dailyword.business.data.network.abstraction.WordNetworkDataSource
import com.pramod.dailyword.business.data.network.impl.IPInfoNetworkDataSourceImpl
import com.pramod.dailyword.business.data.network.impl.WordNetworkDataSourceImpl
import com.pramod.dailyword.framework.datasource.network.abstraction.IPNetworkService
import com.pramod.dailyword.framework.datasource.network.abstraction.WordNetworkService
import com.pramod.dailyword.framework.datasource.network.impl.IPNetworkServiceImpl
import com.pramod.dailyword.framework.datasource.network.impl.WordNetworkServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(value = [SingletonComponent::class])
interface NetworkDataSourceModule {

    @Binds
    fun provideWordNetworkService(
        wordNetworkServiceImpl: WordNetworkServiceImpl
    ): WordNetworkService


    @Binds
    fun provideIPNetworkService(
        iPNetworkServiceImple: IPNetworkServiceImpl
    ): IPNetworkService


    @Binds
    fun provideWordNetworkDataSource(wordNetworkDataSourceImpl: WordNetworkDataSourceImpl): WordNetworkDataSource

    @Binds
    fun provideIPInfoNetworkDataSource(iPInfoNetworkDataSourceImpl: IPInfoNetworkDataSourceImpl): IPInfoNetworkDataSource

}