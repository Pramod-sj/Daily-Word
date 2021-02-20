package com.pramod.dailyword.framework.datasource.cache.impl

import com.pramod.dailyword.business.domain.model.Bookmark
import com.pramod.dailyword.framework.datasource.cache.abstraction.BookmarkCacheService
import com.pramod.dailyword.framework.datasource.cache.dao.BookmarkDao
import com.pramod.dailyword.framework.datasource.cache.mappers.BookmarkCEMapper

class BookmarkCacheServiceImpl(
    private val bookmarkDao: BookmarkDao,
    private val bookmarkCEMapper: BookmarkCEMapper
) : BookmarkCacheService {
    override suspend fun insert(bookmark: Bookmark): Long {
        return bookmarkDao.insert(bookmarkCEMapper.toEntity(bookmark))
    }

    override suspend fun delete(word: String): Int {
        return bookmarkDao.delete(word)
    }
}