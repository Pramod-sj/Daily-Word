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
import com.pramod.dailyword.framework.helper.CountryCodeFinder
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

    @Provides
    @Singleton
    fun provideAudioPlayer(
        @ApplicationContext context: Context
    ): AudioPlayer {
        return AudioPlayer(context)
    }

    @Provides
    @Singleton
    fun provideAutoStartPermissionHelper(): AutoStartPermissionHelper {
        return AutoStartPermissionHelper.getInstance()
    }

}