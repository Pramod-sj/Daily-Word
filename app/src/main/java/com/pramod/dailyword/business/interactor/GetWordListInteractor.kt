package com.pramod.dailyword.business.interactor

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkedWordCacheDataSource
import com.pramod.dailyword.business.domain.model.Word
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetWordListInteractor @ExperimentalPagingApi @Inject constructor(
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