package com.pramod.dailyword.business.interactor

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkedWordCacheDataSource
import com.pramod.dailyword.business.data.cache.abstraction.WordCacheDataSource
import com.pramod.dailyword.business.data.network.NetworkBoundResource
import com.pramod.dailyword.business.data.network.Resource
import com.pramod.dailyword.business.data.network.abstraction.WordNetworkDataSource
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.framework.datasource.network.model.api.ApiResponse
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetWordDetailsByDateInteractor @Inject  constructor(
    private val bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource,
    private val wordCacheDataSource: WordCacheDataSource,
    private val wordNetworkDataSource: WordNetworkDataSource
) {
    fun getWordDetailsByDate(
        date: String,
        forceRefresh: Boolean = false
    ): Flow<Resource<Word?>> {
        return object : NetworkBoundResource<List<Word>, Word>() {

            override suspend fun fetchFromCache(): Flow<Word?> =
                bookmarkedWordCacheDataSource.getWordByDateAsFlow(date)

            override fun shouldFetchFromNetwork(data: Word?): Boolean = forceRefresh

            override suspend fun saveIntoCache(data: List<Word>) {
                wordCacheDataSource.addAll(data)
            }

            override suspend fun fetchFromNetwork(): ApiResponse<List<Word>> {
                return wordNetworkDataSource.getWords(date, 1)
            }

        }.asFlow()
    }

}