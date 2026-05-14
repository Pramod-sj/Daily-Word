package com.pramod.dailyword.framework.datasource.cache.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.pramod.dailyword.business.data.network.paging.WordPaginationRemoteMediator
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.framework.datasource.cache.abstraction.BookmarkedWordCacheService
import com.pramod.dailyword.framework.datasource.cache.dao.BookmarkedWordDao
import com.pramod.dailyword.framework.datasource.cache.mappers.BookmarkedWordCEMapperV2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkedWordCacheServiceImpl @Inject constructor(
    private val bookmarkedWordDao: BookmarkedWordDao,
    private val bookmarkedWordCEMapper: BookmarkedWordCEMapperV2
) : BookmarkedWordCacheService {
    override fun getWordByDate(date: String): LiveData<Word?> {
        return bookmarkedWordDao.getWordByDate(date)
            .asFlow()
            .map { it?.let { bookmarkedWordCEMapper.fromEntity(it) } }
            .asLiveData(Dispatchers.Default)
    }

    override fun getWordByDateAsFlow(date: String): Flow<Word?> {
        return bookmarkedWordDao.getWordByDateAsFlow(date)
            .map {
                it?.let { bookmarkedWordCEMapper.fromEntity(it) }
            }
    }


    override fun getWordByName(word: String): LiveData<Word?> {
        return bookmarkedWordDao.getWordByName(word)
            .asFlow()
            .map {
                it?.let { bookmarkedWordCEMapper.fromEntity(it) }
            }.asLiveData(Dispatchers.Default)
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
        return bookmarkedWordDao.getFewExceptTopOneWord(count)
            .asFlow()
            .map {
                it?.map { wordCE -> bookmarkedWordCEMapper.fromEntity(wordCE) }
            }.asLiveData(Dispatchers.Default)

    }

    override fun getFewWordsFromTop(count: Int): LiveData<List<Word>?> {
        return bookmarkedWordDao.getFewWordsFromTop(count)
            .asFlow().map {
                it?.map { wordCE -> bookmarkedWordCEMapper.fromEntity(wordCE) }
            }.asLiveData(Dispatchers.Default)
    }

    override fun getFewWordsFromTopAsFlow(count: Int): Flow<List<Word>?> {
        return bookmarkedWordDao.getFewWordsFromTopAsFlow(count).map {
            it?.map { wordCE -> bookmarkedWordCEMapper.fromEntity(wordCE) }
        }
    }

    override fun getFewWordsTill(tillDate: Long, count: Int): LiveData<List<Word>?> {
        return bookmarkedWordDao.getFewWordsTill(tillDate, count)
            .asFlow()
            .map {
                it?.map { wordCE -> bookmarkedWordCEMapper.fromEntity(wordCE) }
            }.asLiveData(Dispatchers.Default)
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
        return bookmarkedWordDao.getAllExcept(date)
            .asFlow()
            .map {
                it?.map { wordCE -> bookmarkedWordCEMapper.fromEntity(wordCE) }
            }.asLiveData(Dispatchers.Default)

    }

    override fun getFewExcept(date: String, count: Int): LiveData<List<Word>?> {
        return bookmarkedWordDao.getFewExcept(date, count)
            .asFlow().map {
                it?.map { wordCE -> bookmarkedWordCEMapper.fromEntity(wordCE) }
            }.asLiveData(Dispatchers.Default)
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


    @OptIn(ExperimentalPagingApi::class)
    override fun getWordsPagingSource(
        pageConfig: PagingConfig,
        remoteMediator: WordPaginationRemoteMediator
    ): Flow<PagingData<Word>> {
        return Pager(pageConfig, remoteMediator = remoteMediator) {
            bookmarkedWordDao.getWordsPagingSource(remoteMediator.search)
        }.flow.map { pagingData ->
            pagingData.map {
                bookmarkedWordCEMapper.fromEntity(it)
            }
        }
    }

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