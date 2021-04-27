package com.pramod.dailyword.business.data.cache.impl

import androidx.lifecycle.LiveData
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkCacheDataSource
import com.pramod.dailyword.business.domain.model.Bookmark
import com.pramod.dailyword.framework.datasource.cache.abstraction.BookmarkCacheService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkCacheDataSourceImpl @Inject constructor(private val bookmarkCacheService: BookmarkCacheService) :
    BookmarkCacheDataSource {
    override suspend fun insert(bookmark: Bookmark): Long {
        return bookmarkCacheService.insert(bookmark)
    }

    override suspend fun update(bookmark: Bookmark): Int {
        return bookmarkCacheService.update(bookmark)
    }

    override suspend fun get(word: String): Bookmark? {
        return bookmarkCacheService.get(word)
    }

    override fun getAll(): LiveData<List<Bookmark>?> {
        return bookmarkCacheService.getAll()
    }

    override suspend fun delete(word: String): Int {
        return bookmarkCacheService.delete(word)
    }
}