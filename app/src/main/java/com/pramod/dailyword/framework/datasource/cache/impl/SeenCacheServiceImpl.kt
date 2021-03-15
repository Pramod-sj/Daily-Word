package com.pramod.dailyword.framework.datasource.cache.impl

import com.pramod.dailyword.business.domain.model.Seen
import com.pramod.dailyword.framework.datasource.cache.abstraction.SeenCacheService
import com.pramod.dailyword.framework.datasource.cache.dao.SeenDao
import com.pramod.dailyword.framework.datasource.cache.mappers.SeenCEMapper

class SeenCacheServiceImpl(
    private val seenDao: SeenDao,
    private val seenCEMapper: SeenCEMapper
) : SeenCacheService {
    override suspend fun add(seen: Seen): Long {
        return seenDao.add(seenCEMapper.toEntity(seen))
    }

    override suspend fun update(seen: Seen): Int {
        return seenDao.update(seenCEMapper.toEntity(seen))
    }

    override suspend fun getAll(): List<Seen> {
        return seenDao.getAll().map {
            seenCEMapper.fromEntity(it)
        }
    }

    override suspend fun get(word: String): Seen? {
        return seenDao.get(word)?.let { seenCEMapper.fromEntity(it) }
    }

    override suspend fun delete(word: String): Int {
        return seenDao.delete(word)
    }
}