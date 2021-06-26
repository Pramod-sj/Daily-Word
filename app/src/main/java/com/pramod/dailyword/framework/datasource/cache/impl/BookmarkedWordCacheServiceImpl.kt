package com.pramod.dailyword.framework.datasource.cache.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.paging.*
import com.pramod.dailyword.business.data.network.paging.WordPaginationRemoteMediator
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.framework.datasource.cache.abstraction.BookmarkedWordCacheService
import com.pramod.dailyword.framework.datasource.cache.dao.BookmarkedWordDao
import com.pramod.dailyword.framework.datasource.cache.mappers.BookmarkedWordCEMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkedWordCacheServiceImpl @Inject constructor(
    private val bookmarkedWordDao: BookmarkedWordDao,
    private val bookmarkedWordCEMapper: BookmarkedWordCEMapper
) : BookmarkedWordCacheService {
    override fun getWordByDate(date: String): LiveData<Word?> {
        return bookmarkedWordDao.getWordByDate(date)
            .map { it?.let { bookmarkedWordCEMapper.fromEntity(it) } }
    }

    override fun getWordByDateAsFlow(date: String): Flow<Word?> {
        return bookmarkedWordDao.getWordByDateAsFlow(date)
            .map {
                it?.let { bookmarkedWordCEMapper.fromEntity(it) }
            }
    }


    override fun getWordByName(word: String): LiveData<Word?> {
        return bookmarkedWordDao.getWordByName(word)
            .map {
                it?.let { bookmarkedWordCEMapper.fromEntity(it) }
            }
    }

    override fun getWordByNameFlow(word: String): Flow<Word?> {
        return bookmarkedWordDao.getWordByNameFlow(word)
            .map {
                it?.let { bookmarkedWordCEMapper.fromEntity(it) }
            }
    }

    override fun getTopOneWord(): Flow<Word?> {
        return bookmarkedWordDao.getTopOneWord().map {
            it?.let { bookmarkedWordCEMapper.fromEntity(it) }
        }
    }

    override fun getFewExceptTopOneWord(count: Int): LiveData<List<Word>?> {
        return bookmarkedWordDao.getFewExceptTopOneWord(count).map {
            it?.map { wordCE -> bookmarkedWordCEMapper.fromEntity(wordCE) }
        }
    }

    override fun getFewWordsFromTop(count: Int): LiveData<List<Word>?> {
        return bookmarkedWordDao.getFewWordsFromTop(count).map {
            it?.map { wordCE -> bookmarkedWordCEMapper.fromEntity(wordCE) }
        }
    }

    override fun getFewWordsFromTopAsFlow(count: Int): Flow<List<Word>?> {
        return bookmarkedWordDao.getFewWordsFromTopAsFlow(count).map {
            it?.map { wordCE -> bookmarkedWordCEMapper.fromEntity(wordCE) }
        }
    }

    override fun getFewWordsTill(tillDate: Long, count: Int): LiveData<List<Word>?> {
        return bookmarkedWordDao.getFewWordsTill(tillDate, count).map {
            it?.map { wordCE -> bookmarkedWordCEMapper.fromEntity(wordCE) }
        }
    }

    override fun getFewWordsTillAsFlow(
        fromDate: Long,
        tillDate: Long,
        count: Int
    ): Flow<List<Word>?> {
        return bookmarkedWordDao.getFewWordsTillAsFlow(fromDate, tillDate, count).map {
            it?.map { wordCE -> bookmarkedWordCEMapper.fromEntity(wordCE) }
        }
    }

    override fun getAllExcept(date: String): LiveData<List<Word>?> {
        return bookmarkedWordDao.getAllExcept(date).map {
            it?.map { wordCE -> bookmarkedWordCEMapper.fromEntity(wordCE) }
        }
    }

    override fun getFewExcept(date: String, count: Int): LiveData<List<Word>?> {
        return bookmarkedWordDao.getFewExcept(date, count).map {
            it?.map { wordCE -> bookmarkedWordCEMapper.fromEntity(wordCE) }
        }
    }

    override suspend fun getWordNonLive(date: String): Word? {
        return bookmarkedWordDao.getWordNonLive(date)?.let { bookmarkedWordCEMapper.fromEntity(it) }
    }

    override suspend fun getWordByNameNonLive(word: String): Word? {
        return bookmarkedWordDao.getWordByNameNonLive(word)
            ?.let { bookmarkedWordCEMapper.fromEntity(it) }
    }

    override suspend fun getJustTopOneWordNonLive(): Word? {
        return bookmarkedWordDao.getJustTopOneWordNonLive()
            ?.let { bookmarkedWordCEMapper.fromEntity(it) }
    }


    @ExperimentalPagingApi
    override fun getWordsPagingSource(
        pageConfig: PagingConfig,
        remoteMediator: WordPaginationRemoteMediator
    ): Flow<PagingData<Word>> {
        return Pager(pageConfig, remoteMediator = remoteMediator) {
            bookmarkedWordDao.getWordsPagingSource()
        }.flow.map { pagingData ->
            pagingData.map {
                bookmarkedWordCEMapper.fromEntity(it)
            }
        }
    }

    @ExperimentalPagingApi
    override fun getBookmarkedWordsPagingSource(pagingConfig: PagingConfig): Flow<PagingData<Word>> {
        return Pager(pagingConfig) {
            bookmarkedWordDao.getBookmarksPagingDataSource()
        }.flow.map { pagingData ->
            pagingData.map {
                bookmarkedWordCEMapper.fromEntity(it)
            }
        }
    }

}