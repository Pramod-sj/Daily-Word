package com.pramod.dailyword.framework.datasource.cache.abstraction

import com.pramod.dailyword.business.domain.model.Word

interface WordCacheService {
    suspend fun addAll(word: List<Word>): List<Long>

    suspend fun add(word: Word): Long

    suspend fun get(wordDate: String): Word?

    suspend fun getAll(): List<Word>?

    suspend fun update(word: Word): Int

    suspend fun delete(word: String): Int

    suspend fun deleteAll(): Int
}