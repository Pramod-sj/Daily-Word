package com.pramod.dailyword.di

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import com.judemanutd.autostarter.AutoStartPermissionHelper
import com.library.audioplayer.AudioPlayer
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkedWordCacheDataSource
import com.pramod.dailyword.business.data.cache.abstraction.WordCacheDataSource
import com.pramod.dailyword.business.data.network.abstraction.IPInfoNetworkDataSource
import com.pramod.dailyword.business.data.network.abstraction.WordNetworkDataSource
import com.pramod.dailyword.business.interactor.WordPaginationRemoteMediator
import com.pramod.dailyword.business.interactor.bookmark.GetAllBookmarks
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.firebase.FBTopicSubscriber
import com.pramod.dailyword.framework.helper.AdsManager
import com.pramod.dailyword.framework.helper.AppUpdateHelper
import com.pramod.dailyword.framework.helper.NotificationHelper
import com.pramod.dailyword.framework.prefmanagers.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
        wordNetworkDataSource: WordNetworkDataSource,
        remoteKeyPrefManager: RemoteKeyPrefManager
    ): WordPaginationRemoteMediator {
        return WordPaginationRemoteMediator(
            wordNetworkDataSource,
            wordCacheDataSource,
            remoteKeyPrefManager
        )
    }

    @Provides
    fun provideWindowAnimationPrefManager(
        @ApplicationContext context: Context
    ): WindowAnimPrefManager {
        return WindowAnimPrefManager.newInstance(context)
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


    @Provides
    @Singleton
    fun provideAudioPlayer(
        @ApplicationContext context: Context
    ): AudioPlayer {
        return AudioPlayer(context)
    }

    @Provides
    @Singleton
    fun provideAdsManager(
        @ApplicationContext context: Context
    ): AdsManager {
        return AdsManager.newInstance(context)
    }

    @Provides
    @Singleton
    fun provideHomeScreenBadgeManager(
        @ApplicationContext context: Context,
        bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource,
        getAllBookmarks: GetAllBookmarks,
        prefManager: PrefManager
    ): HomeScreenBadgeManager {
        return HomeScreenBadgeManager(
            context,
            bookmarkedWordCacheDataSource,
            getAllBookmarks,
            prefManager
        )
    }

    @Provides
    @Singleton
    fun provideRemoteKeyPrefManager(@ApplicationContext context: Context): RemoteKeyPrefManager {
        return RemoteKeyPrefManager(context)
    }


    @Provides
    @Singleton
    fun provideAppUpdateHelper(@ApplicationContext context: Context): AppUpdateHelper {
        return AppUpdateHelper(context)
    }

    @Provides
    @Singleton
    fun provideAutoStartPermissionHelper(): AutoStartPermissionHelper {
        return AutoStartPermissionHelper.getInstance()
    }


    @Provides
    fun provideNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper {
        return NotificationHelper(context)
    }
}