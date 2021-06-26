package com.pramod.dailyword.business.interactor

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkedWordCacheDataSource
import com.pramod.dailyword.business.data.cache.abstraction.WordCacheDataSource
import com.pramod.dailyword.business.data.network.abstraction.WordNetworkDataSource
import com.pramod.dailyword.business.data.network.paging.WordPaginationRemoteMediator
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.framework.prefmanagers.RemoteKeyPrefManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetWordPagingInteractor @ExperimentalPagingApi @Inject constructor(
    private val bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource,
    private val wordNetworkDataSource: WordNetworkDataSource,
    private val wordCacheDataSource: WordCacheDataSource,
    private val remoteKeyPrefManager: RemoteKeyPrefManager
) {
    @ExperimentalPagingApi
    fun getWordList(search: String, pagingConfig: PagingConfig): Flow<PagingData<Word>> {

        return flow {
            val mediator = WordPaginationRemoteMediator(
                search = search,
                //skip initial refresh when data in local is more then PAGE_SIZE
                skipInitialRefresh = (wordCacheDataSource.getAll()?.size
                    ?: 0) > pagingConfig.pageSize,
                wordNetworkDataSource = wordNetworkDataSource,
                wordCacheDataSource = wordCacheDataSource,
                remoteKeyPrefManager = remoteKeyPrefManager
            )

            emitAll(
                bookmarkedWordCacheDataSource.getWordsPagingSource(
                    pagingConfig,
                    mediator
                )
            )
        }.flowOn(Dispatchers.Default)
    }
}