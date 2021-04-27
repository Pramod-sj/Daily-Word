package com.pramod.dailyword.business.interactor

import com.pramod.dailyword.business.data.cache.abstraction.BookmarkedWordCacheDataSource
import com.pramod.dailyword.business.data.cache.abstraction.WordCacheDataSource
import com.pramod.dailyword.business.data.network.Resource
import com.pramod.dailyword.business.data.network.Status
import com.pramod.dailyword.business.data.network.abstraction.WordNetworkDataSource
import com.pramod.dailyword.business.data.network.utils.ApiResponseHandler
import com.pramod.dailyword.business.data.network.utils.safeApiCall
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.framework.datasource.network.model.api.ApiResponse
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetRandomWordInteractor @Inject constructor(
    private val bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource,
    private val wordCacheDataSource: WordCacheDataSource,
    private val wordNetworkDataSource: WordNetworkDataSource
) {
    fun getRandomWord(): Flow<Resource<Word?>> {
        return flow {
            emit(Resource.loading())
            val apiResponse = safeApiCall(Dispatchers.IO) {
                wordNetworkDataSource.getRandomWord()
            }
            val resource =
                object : ApiResponseHandler<ApiResponse<Word>, Word>(apiResponse) {
                    override suspend fun handleSuccess(resultObj: ApiResponse<Word>): Resource<Word?> {
                        return if (resultObj.code == "200") {
                            resultObj.data?.let {
                                wordCacheDataSource.add(it)
                            }
                            Resource.success(resultObj.data)
                        } else {
                            Resource.error(Throwable(resultObj.message), null)
                        }
                    }
                }.getResult()
            if (resource.status == Status.SUCCESS) {
                resource.data?.date?.let {
                    emitAll(bookmarkedWordCacheDataSource.getWordByDateAsFlow(it).map { word ->
                        Resource.success(word)
                    })
                }
            } else {
                emit(resource)
            }

        }

    }
}