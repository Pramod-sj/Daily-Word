package com.pramod.dailyword.business.data.cache.impl

import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkedWordCacheDataSource
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.business.interactor.WordPaginationRemoteMediator
import com.pramod.dailyword.framework.datasource.cache.abstraction.BookmarkedWordCacheService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkedWordCacheDataSourceImpl @Inject constructor(private val bookmarkedWordCacheService: BookmarkedWordCacheService) :
    BookmarkedWordCacheDataSource {
    override fun getWordByDate(date: String): LiveData<Word?> {
        return bookmarkedWordCacheService.getWordByDate(date)
    }

    override fun getWordByDateAsFlow(date: String): Flow<Word?> {
        return bookmarkedWordCacheService.getWordByDateAsFlow(date)
    }


    override fun getWordByName(word: String): LiveData<Word?> {
        return bookmarkedWordCacheService.getWordByName(word)

    }

    override fun getWordByNameFlow(word: String): Flow<Word?> {
        return bookmarkedWordCacheService.getWordByNameFlow(word)
    }

    override fun getTopOneWord(): Flow<Word?> {
        return bookmarkedWordCacheService.getTopOneWord()
    }

    override fun getFewExceptTopOneWord(count: Int): LiveData<List<Word>?> {
        return bookmarkedWordCacheService.getFewExceptTopOneWord(count)
    }

    override fun getFewWordsFromTop(count: Int): LiveData<List<Word>?> {
        return bookmarkedWordCacheService.getFewWordsFromTop(count)
    }

    override fun getFewWordsFromTopAsFlow(count: Int): Flow<List<Word>?> {
        return bookmarkedWordCacheService.getFewWordsFromTopAsFlow(count)
    }

    override fun getFewWordsTill(tillDate: Long, count: Int): LiveData<List<Word>?> {
        return bookmarkedWordCacheService.getFewWordsTill(tillDate, count)
    }

    override fun getFewWordsTillAsFlow(
        fromDate: Long,
        tillDate: Long,
        count: Int
    ): Flow<List<Word>?> {
        return bookmarkedWordCacheService.getFewWordsTillAsFlow(fromDate, tillDate, count)
    }

    override fun getAllExcept(date: String): LiveData<List<Word>?> {
        return bookmarkedWordCacheService.getAllExcept(date)
    }

    override fun getFewExcept(date: String, count: Int): LiveData<List<Word>?> {
        return bookmarkedWordCacheService.getFewExcept(date, count)
    }

    override suspend fun getWordNonLive(date: String): Word? {
        return bookmarkedWordCacheService.getWordNonLive(date)
    }

    override suspend fun getWordByNameNonLive(word: String): Word? {
        return bookmarkedWordCacheService.getWordByNameNonLive(word)
    }

    override suspend fun getJustTopOneWordNonLive(): Word? {
        return bookmarkedWordCacheService.getJustTopOneWordNonLive()
    }

    @ExperimentalPagingApi
    override fun getWordsPagingSource(
        pagingConfig: PagingConfig,
        remoteMediator: WordPaginationRemoteMediator
    ): Flow<PagingData<Word>> {
        return bookmarkedWordCacheService.getWordsPagingSource(pagingConfig, remoteMediator)
    }

    @ExperimentalPagingApi
    override fun getBookmarkedWordsPagingSource(pagingConfig: PagingConfig): Flow<PagingData<Word>> {
        return bookmarkedWordCacheService.getBookmarkedWordsPagingSource(pagingConfig)
    }
}