package com.pramod.dailyword.business.data.cache.abstraction

import androidx.lifecycle.LiveData
import com.pramod.dailyword.business.domain.model.Bookmark

interface BookmarkCacheDataSource {
    suspend fun insert(bookmark: Bookmark): Long

    suspend fun update(bookmark: Bookmark): Int

    suspend fun get(word: String): Bookmark?

    fun getAll(): LiveData<List<Bookmark>?>

    suspend fun delete(word: String): Int
}