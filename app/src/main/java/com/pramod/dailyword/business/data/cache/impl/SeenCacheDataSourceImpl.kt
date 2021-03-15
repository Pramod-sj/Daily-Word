package com.pramod.dailyword.business.data.cache.impl

import com.pramod.dailyword.business.data.cache.abstraction.SeenCacheDataSource
import com.pramod.dailyword.business.domain.model.Seen
import com.pramod.dailyword.framework.datasource.cache.abstraction.SeenCacheService

class SeenCacheDataSourceImpl(
    private val seenCacheService: SeenCacheService
) : SeenCacheDataSource {
    override suspend  fun add(seen: Seen): Long {
        return seenCacheService.add(seen)
    }

    override suspend fun update(seen: Seen): Int {
        return seenCacheService.update(seen)
    }

    override suspend fun getAll(): List<Seen> {
        return seenCacheService.getAll()
    }

    override suspend fun get(word: String): Seen? {
        return seenCacheService.get(word)
    }

    override suspend fun delete(word: String): Int {
        return seenCacheService.delete(word)
    }
}