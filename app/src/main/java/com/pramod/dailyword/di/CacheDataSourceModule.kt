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
import com.pramod.dailyword.framework.datasource.cache.impl.BookmarkCacheServiceImpl
import com.pramod.dailyword.framework.datasource.cache.impl.BookmarkedWordCacheServiceImpl
import com.pramod.dailyword.framework.datasource.cache.impl.SeenCacheServiceImpl
import com.pramod.dailyword.framework.datasource.cache.impl.WordCacheServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(value = [SingletonComponent::class])
interface CacheDataSourceModule {

    @Binds
    fun provideWordCacheService(wordCacheServiceImpl: WordCacheServiceImpl): WordCacheService

    @Binds
    fun provideBookmarkCacheService(
        bookmarkCacheServiceImpl: BookmarkCacheServiceImpl
    ): BookmarkCacheService


    @Binds
    fun provideBookmarkedWordCacheService(
        bookmarkedWordCacheServiceImpl: BookmarkedWordCacheServiceImpl
    ): BookmarkedWordCacheService

    @Binds
    fun provideSeenCacheService(seenCacheServiceImpl: SeenCacheServiceImpl): SeenCacheService


    @Binds
    fun provideWordCacheDataSource(
        wordCacheDataSourceImpl: WordCacheDataSourceImpl
    ): WordCacheDataSource


    @Binds
    fun provideBookmarkedWordCacheDataSource(
        bookmarkedWordCacheDataSourceImpl: BookmarkedWordCacheDataSourceImpl
    ): BookmarkedWordCacheDataSource

    @Binds
    fun provideBookmarkCacheDataSource(
        bookmarkCacheDataSourceImpl: BookmarkCacheDataSourceImpl
    ): BookmarkCacheDataSource


    @Binds
    fun provideSeenCacheDataSource(
        seenCacheDataSourceImpl: SeenCacheDataSourceImpl
    ): SeenCacheDataSource

}