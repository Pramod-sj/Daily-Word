package com.pramod.dailyword.di

import com.pramod.dailyword.business.data.cache.abstraction.BookmarkCacheDataSource
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkedWordCacheDataSource
import com.pramod.dailyword.business.data.cache.abstraction.SeenCacheDataSource
import com.pramod.dailyword.business.data.cache.abstraction.WordCacheDataSource
import com.pramod.dailyword.business.data.cache.impl.BookmarkCacheDataSourceImpl
import com.pramod.dailyword.business.data.cache.impl.BookmarkedWordCacheDataSourceImpl
import com.pramod.dailyword.business.data.cache.impl.SeenCacheDataSourceImpl
import com.pramod.dailyword.business.data.cache.impl.WordCacheDataSourceImpl
import com.pramod.dailyword.framework.datasource.cache.abstraction.BookmarkCacheService
import com.pramod.dailyword.framework.datasource.cache.abstraction.BookmarkedWordCacheService
import com.pramod.dailyword.framework.datasource.cache.abstraction.SeenCacheService
import com.pramod.dailyword.framework.datasource.cache.abstraction.WordCacheService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(value = [SingletonComponent::class])
object CacheDataSourceModule {
    @JvmStatic
    @Singleton
    @Provides
    fun provideWordCacheDataSource(wordCacheService: WordCacheService): WordCacheDataSource {
        return WordCacheDataSourceImpl(wordCacheService)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideBookmarkedWordCacheDataSource(bookmarkedWordCacheService: BookmarkedWordCacheService): BookmarkedWordCacheDataSource {
        return BookmarkedWordCacheDataSourceImpl(bookmarkedWordCacheService)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideBookmarkCacheDataSource(bookmarkCacheService: BookmarkCacheService): BookmarkCacheDataSource {
        return BookmarkCacheDataSourceImpl(bookmarkCacheService)
    }

    @Singleton
    @Provides
    fun provideSeenCacheDataSource(seenCacheService: SeenCacheService): SeenCacheDataSource {
        return SeenCacheDataSourceImpl(seenCacheService)
    }
}