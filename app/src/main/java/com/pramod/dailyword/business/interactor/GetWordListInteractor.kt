package com.pramod.dailyword.business.interactor

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkedWordCacheDataSource
import com.pramod.dailyword.business.domain.model.Word
import kotlinx.coroutines.flow.Flow

class GetWordListInteractor @ExperimentalPagingApi constructor(
    private val wordPaginationRemoteMediator: WordPaginationRemoteMediator,
    private val bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource
) {
    @ExperimentalPagingApi
    fun getWordList(pagingConfig: PagingConfig): Flow<PagingData<Word>> {
        return bookmarkedWordCacheDataSource.getWordsPagingSource(
            pagingConfig,
            wordPaginationRemoteMediator
        )
    }
}