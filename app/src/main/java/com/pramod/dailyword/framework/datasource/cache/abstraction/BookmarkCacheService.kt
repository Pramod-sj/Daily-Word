package com.pramod.dailyword.framework.datasource.cache.abstraction

import com.pramod.dailyword.business.domain.model.Bookmark


interface BookmarkCacheService {
    suspend fun insert(bookmark: Bookmark): Long

    suspend fun delete(word: String): Int
}