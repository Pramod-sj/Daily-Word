package com.pramod.dailyword.business.interactor

import com.pramod.dailyword.business.data.cache.abstraction.BookmarkedWordCacheDataSource
import com.pramod.dailyword.business.data.cache.abstraction.WordCacheDataSource
import com.pramod.dailyword.business.data.network.NetworkBoundResource
import com.pramod.dailyword.business.data.network.Resource
import com.pramod.dailyword.business.data.network.abstraction.WordNetworkDataSource
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.framework.datasource.network.model.api.ApiResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetWordsInteractor @Inject constructor(
    private val bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource,
    private val wordCacheDataSource: WordCacheDataSource,
    private val wordNetworkDataSource: WordNetworkDataSource
) {

    fun getWords(count: Int, forceFetch: Boolean = true): Flow<Resource<List<Word>?>> {
        return object : NetworkBoundResource<List<Word>, List<Word>>() {
            override suspend fun fetchFromCache(): Flow<List<Word>?> {
                return bookmarkedWordCacheDataSource.getFewWordsFromTopAsFlow(count)
            }

            override fun shouldFetchFromNetwork(data: List<Word>?): Boolean {
                return data?.size ?: 0 < count || forceFetch
            }

            override suspend fun saveIntoCache(data: List<Word>) {
                wordCacheDataSource.addAll(data)
            }

            override suspend fun fetchFromNetwork(): ApiResponse<List<Word>> {
                return wordNetworkDataSource.getWords(startFrom = null, limit = count)
            }

        }.asFlow()
    }

}