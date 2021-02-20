package com.pramod.dailyword.business.data.cache.abstraction

import com.pramod.dailyword.business.domain.model.Bookmark

interface BookmarkCacheDataSource {
    suspend fun insert(bookmark: Bookmark): Long

    suspend fun delete(word: String): Int
}