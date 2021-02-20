package com.pramod.dailyword.di

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import com.pramod.dailyword.business.data.cache.abstraction.WordCacheDataSource
import com.pramod.dailyword.business.data.network.abstraction.IPInfoNetworkDataSource
import com.pramod.dailyword.business.data.network.abstraction.WordNetworkDataSource
import com.pramod.dailyword.business.interactor.WordPaginationRemoteMediator
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.firebase.FBTopicSubscriber
import com.pramod.dailyword.framework.prefmanagers.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(value = [SingletonComponent::class])
object AppModule {

    @ExperimentalPagingApi
    @Provides
    fun provideWordPagingRemoteMediator(
        wordCacheDataSource: WordCacheDataSource,
        wordNetworkDataSource: WordNetworkDataSource
    ): WordPaginationRemoteMediator {
        return WordPaginationRemoteMediator(wordNetworkDataSource, wordCacheDataSource)
    }

    @Provides
    fun provideWindowAnimationPrefManager(
        @ApplicationContext context: Context
    ): WindowAnimationPrefManager {
        return WindowAnimationPrefManager.newInstance(context)
    }

    @Provides
    fun provideAutoStartPrefManager(
        @ApplicationContext context: Context
    ): AutoStartPrefManager {
        return AutoStartPrefManager.newInstance(context)
    }

    @Provides
    fun provideFBTopicSubscriber(
        @ApplicationContext context: Context,
        ipInfoNetworkDataSource: IPInfoNetworkDataSource
    ): FBTopicSubscriber {
        return FBTopicSubscriber(context, ipInfoNetworkDataSource)
    }


    @Provides
    fun provideNotificationPrefManager(
        @ApplicationContext context: Context
    ): NotificationPrefManager {
        return NotificationPrefManager.newInstance(context)
    }


    @Provides
    @Singleton
    fun provideFBRemoteConfig(): FBRemoteConfig {
        return FBRemoteConfig()
    }


    @Provides
    @Singleton
    fun providePrefManager(
        @ApplicationContext context: Context
    ): PrefManager {
        return PrefManager(context)
    }
}