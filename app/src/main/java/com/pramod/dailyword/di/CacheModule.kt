package com.pramod.dailyword.di

import android.content.Context
import com.pramod.dailyword.framework.datasource.cache.AppDB
import com.pramod.dailyword.framework.datasource.cache.abstraction.BookmarkCacheService
import com.pramod.dailyword.framework.datasource.cache.abstraction.BookmarkedWordCacheService
import com.pramod.dailyword.framework.datasource.cache.abstraction.SeenCacheService
import com.pramod.dailyword.framework.datasource.cache.abstraction.WordCacheService
import com.pramod.dailyword.framework.datasource.cache.dao.BookmarkDao
import com.pramod.dailyword.framework.datasource.cache.dao.BookmarkedWordDao
import com.pramod.dailyword.framework.datasource.cache.dao.SeenDao
import com.pramod.dailyword.framework.datasource.cache.dao.WordDao
import com.pramod.dailyword.framework.datasource.cache.impl.BookmarkCacheServiceImpl
import com.pramod.dailyword.framework.datasource.cache.impl.BookmarkedWordCacheServiceImpl
import com.pramod.dailyword.framework.datasource.cache.impl.SeenCacheServiceImpl
import com.pramod.dailyword.framework.datasource.cache.impl.WordCacheServiceImpl
import com.pramod.dailyword.framework.datasource.cache.mappers.BookmarkCEMapper
import com.pramod.dailyword.framework.datasource.cache.mappers.BookmarkedWordCEMapper
import com.pramod.dailyword.framework.datasource.cache.mappers.SeenCEMapper
import com.pramod.dailyword.framework.datasource.cache.mappers.WordCEMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(value = [SingletonComponent::class])
object CacheModule {
    @JvmStatic
    @Singleton
    @Provides
    fun provideDailyWordDatabase(@ApplicationContext context: Context): AppDB {
        return AppDB.getInstance(context)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideWordDao(appDB: AppDB): WordDao {
        return appDB.getWordOfTheDayDao()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideWordCacheService(wordDao: WordDao, wordCEMapper: WordCEMapper): WordCacheService {
        return WordCacheServiceImpl(wordDao, wordCEMapper)
    }


    @JvmStatic
    @Singleton
    @Provides
    fun provideBookmarkDao(appDB: AppDB): BookmarkDao {
        return appDB.getBookmarkDao()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideBookmarkCacheService(
            bookmarkDao: BookmarkDao,
            bookmarkCEMapper: BookmarkCEMapper
    ): BookmarkCacheService {
        return BookmarkCacheServiceImpl(bookmarkDao, bookmarkCEMapper)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideBookmarkedWordDao(appDB: AppDB): BookmarkedWordDao {
        return appDB.getBookmarkedWordDao()
    }


    @JvmStatic
    @Singleton
    @Provides
    fun provideBookmarkedWordCacheService(
            bookmarkedWordDao: BookmarkedWordDao,
            bookmarkedWordCEMapper: BookmarkedWordCEMapper
    ): BookmarkedWordCacheService {
        return BookmarkedWordCacheServiceImpl(bookmarkedWordDao, bookmarkedWordCEMapper)
    }

    @Singleton
    @Provides
    fun provideSeenDao(appDB: AppDB): SeenDao {
        return appDB.getSeenDao()
    }

    @Singleton
    @Provides
    fun provideSeenCacheService(seenDao: SeenDao, seenCEMapper: SeenCEMapper): SeenCacheService {
        return SeenCacheServiceImpl(
                seenDao, seenCEMapper
        )
    }

}