package com.pramod.dailyword.di

import com.pramod.dailyword.business.data.network.abstraction.IPInfoNetworkDataSource
import com.pramod.dailyword.business.data.network.abstraction.WordNetworkDataSource
import com.pramod.dailyword.business.data.network.impl.IPInfoNetworkDataSourceImpl
import com.pramod.dailyword.business.data.network.impl.WordNetworkDataSourceImpl
import com.pramod.dailyword.framework.datasource.network.abstraction.IPNetworkService
import com.pramod.dailyword.framework.datasource.network.abstraction.WordNetworkService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(value = [SingletonComponent::class])
object NetworkDataSourceModule {
    @JvmStatic
    @Singleton
    @Provides
    fun provideWordNetworkDataSource(wordNetworkService: WordNetworkService): WordNetworkDataSource {
        return WordNetworkDataSourceImpl(wordNetworkService)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideIPInfoNetworkDataSource(ipNetworkService: IPNetworkService): IPInfoNetworkDataSource {
        return IPInfoNetworkDataSourceImpl(ipNetworkService)
    }

}