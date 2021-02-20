package com.pramod.dailyword.framework.datasource.cache.impl

import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.framework.datasource.cache.abstraction.WordCacheService
import com.pramod.dailyword.framework.datasource.cache.dao.WordDao
import com.pramod.dailyword.framework.datasource.cache.mappers.WordCEMapper

class WordCacheServiceImpl(private val wordDao: WordDao, private val wordCEMapper: WordCEMapper) :
    WordCacheService {
    override suspend fun addAll(word: List<Word>): List<Long> {
        return wordDao.addAll(word.map { wordCEMapper.toEntity(it) })
    }

    override suspend fun add(word: Word): Long {
        return wordDao.add(wordCEMapper.toEntity(word))
    }

    override suspend fun update(word: Word): Int {
        return wordDao.update(wordCEMapper.toEntity(word))
    }

    override suspend fun delete(word: String): Int {
        return wordDao.delete(word)
    }

    override suspend fun deleteAll(): Int {
        return wordDao.deleteAll()
    }
}