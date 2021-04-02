package com.pramod.dailyword.framework.datasource.cache.abstraction

import com.pramod.dailyword.business.domain.model.Seen

interface SeenCacheService {
    suspend fun add(seen: Seen): Long

    suspend fun update(seen: Seen): Int

    suspend fun getAll(): List<Seen>

    suspend fun get(word: String): Seen?

    suspend fun delete(word: String): Int
}