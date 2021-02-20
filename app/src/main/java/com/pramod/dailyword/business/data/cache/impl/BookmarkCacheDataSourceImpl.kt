package com.pramod.dailyword.business.data.cache.impl

import com.pramod.dailyword.business.data.cache.abstraction.BookmarkCacheDataSource
import com.pramod.dailyword.business.domain.model.Bookmark
import com.pramod.dailyword.framework.datasource.cache.abstraction.BookmarkCacheService

class BookmarkCacheDataSourceImpl(private val bookmarkCacheService: BookmarkCacheService) :
    BookmarkCacheDataSource {
    override suspend fun insert(bookmark: Bookmark): Long {
        return bookmarkCacheService.insert(bookmark)
    }

    override suspend fun delete(word: String): Int {
        return bookmarkCacheService.delete(word)
    }
}