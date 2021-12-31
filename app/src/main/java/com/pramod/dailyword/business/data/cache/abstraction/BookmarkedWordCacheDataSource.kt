package com.pramod.dailyword.business.data.cache.abstraction

import androidx.lifecycle.LiveData
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.pramod.dailyword.business.data.network.paging.WordPaginationRemoteMediator
import com.pramod.dailyword.business.domain.model.Word
import kotlinx.coroutines.flow.Flow

interface BookmarkedWordCacheDataSource {
    fun getWordByDate(date: String): LiveData<Word?>

    fun getWordByDateAsFlow(date: String): Flow<Word?>

    fun getWordByName(word: String): LiveData<Word?>

    fun getWordByNameFlow(word: String): Flow<Word?>

    fun getTopOneWord(): Flow<Word?>

    fun getFewExceptTopOneWord(count: Int): LiveData<List<Word>?>

    fun getFewWordsFromTop(count: Int): LiveData<List<Word>?>

    fun getFewWordsFromTopAsFlow(count: Int): Flow<List<Word>?>

    fun getFewWordsTill(tillDate: Long, count: Int): LiveData<List<Word>?>

    fun getFewWordsTillAsFlow(fromDate: Long, tillDate: Long, count: Int): Flow<List<Word>?>

    fun getAllExcept(date: String): LiveData<List<Word>?>

    fun getFewExcept(date: String, count: Int): LiveData<List<Word>?>

    suspend fun getWordNonLive(date: String): Word?

    suspend fun getWordByNameNonLive(word: String): Word?

    suspend fun getJustTopOneWordNonLive(): Word?

    fun getWordsPagingSource(
        pagingConfig: PagingConfig,
        remoteMediator: WordPaginationRemoteMediator
    ): Flow<PagingData<Word>>

    fun getBookmarkedWordsPagingSource(
        pagingConfig: PagingConfig,
    ): Flow<PagingData<Word>>
}