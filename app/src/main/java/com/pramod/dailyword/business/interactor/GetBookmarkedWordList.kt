package com.pramod.dailyword.business.interactor

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkedWordCacheDataSource
import com.pramod.dailyword.business.domain.model.Word
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetBookmarkedWordList @Inject constructor(
    private val bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource
) {

    fun getBookmarkedWordList(count: Int = 20): Flow<PagingData<Word>> {
        return bookmarkedWordCacheDataSource.getBookmarkedWordsPagingSource(
            PagingConfig(count)
        )
    }
}