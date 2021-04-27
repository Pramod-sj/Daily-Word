package com.pramod.dailyword.framework.datasource.cache.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.pramod.dailyword.business.domain.model.Bookmark
import com.pramod.dailyword.framework.datasource.cache.abstraction.BookmarkCacheService
import com.pramod.dailyword.framework.datasource.cache.dao.BookmarkDao
import com.pramod.dailyword.framework.datasource.cache.mappers.BookmarkCEMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkCacheServiceImpl @Inject constructor(
    private val bookmarkDao: BookmarkDao,
    private val bookmarkCEMapper: BookmarkCEMapper
) : BookmarkCacheService {
    override suspend fun insert(bookmark: Bookmark): Long {
        return bookmarkDao.insert(bookmarkCEMapper.toEntity(bookmark))
    }

    override suspend fun update(bookmark: Bookmark): Int {
        return bookmarkDao.update(bookmarkCEMapper.toEntity(bookmark))
    }

    override suspend fun get(word: String): Bookmark? {
        return bookmarkDao.get(word)?.let {
            bookmarkCEMapper.fromEntity(it)
        }
    }

    override fun getAll(): LiveData<List<Bookmark>?> {
        return bookmarkDao.getAll().map {
            it?.map { bookmarkCE ->
                bookmarkCEMapper.fromEntity(bookmarkCE)
            }
        }
    }

    override suspend fun delete(word: String): Int {
        return bookmarkDao.delete(word)
    }
}