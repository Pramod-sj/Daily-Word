package com.pramod.dailyword.business.data.cache.impl

import com.pramod.dailyword.business.data.cache.abstraction.WordCacheDataSource
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.framework.datasource.cache.abstraction.WordCacheService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordCacheDataSourceImpl @Inject constructor(private val wordCacheService: WordCacheService) :
    WordCacheDataSource {
    override suspend fun addAll(word: List<Word>): List<Long> {
        return wordCacheService.addAll(word)
    }

    override suspend fun add(word: Word): Long {
        return wordCacheService.add(word)
    }

    override suspend fun get(wordDate: String): Word? {
        return wordCacheService.get(wordDate)
    }

    override suspend fun getAll(): List<Word>? {
        return wordCacheService.getAll()
    }

    override suspend fun update(word: Word): Int {
        return wordCacheService.update(word)
    }

    override suspend fun delete(word: String): Int {
        return wordCacheService.delete(word)
    }

    override suspend fun deleteAll(): Int {
        return wordCacheService.deleteAll()
    }

    override suspend fun deleteAllExceptTop(n: Int): Int {
        return wordCacheService.deleteAllExceptTop(n)
    }
}