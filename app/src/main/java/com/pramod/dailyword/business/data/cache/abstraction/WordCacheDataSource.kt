package com.pramod.dailyword.business.data.cache.abstraction

import com.pramod.dailyword.business.domain.model.Word

interface WordCacheDataSource {
    suspend fun addAll(word: List<Word>): List<Long>

    suspend fun add(word: Word): Long

    suspend fun get(wordDate: String): Word?

    suspend fun getWordByName(wordName: String): Word?

    suspend fun getAll(): List<Word>?

    suspend fun update(word: Word): Int

    suspend fun delete(word: String): Int

    suspend fun deleteAll(): Int

    suspend fun deleteAllExceptTop(n: Int): Int
}