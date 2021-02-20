package com.pramod.dailyword.business.interactor

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.pramod.dailyword.business.data.cache.abstraction.WordCacheDataSource
import com.pramod.dailyword.business.data.network.Resource
import com.pramod.dailyword.business.data.network.Status
import com.pramod.dailyword.business.data.network.abstraction.WordNetworkDataSource
import com.pramod.dailyword.business.data.network.utils.ApiResponseHandler
import com.pramod.dailyword.business.data.network.utils.safeApiCall
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.framework.datasource.cache.model.BookmarkedWordCE
import com.pramod.dailyword.framework.datasource.network.model.api.ApiResponse
import com.pramod.dailyword.framework.ui.words.NETWORK_PAGE_SIZE
import com.pramod.dailyword.framework.util.CalenderUtil
import kotlinx.coroutines.Dispatchers
import java.util.*
import javax.inject.Inject

@ExperimentalPagingApi
class WordPaginationRemoteMediator @Inject constructor(
    private val wordNetworkDataSource: WordNetworkDataSource,
    private val wordCacheDataSource: WordCacheDataSource
) : RemoteMediator<Int, BookmarkedWordCE>() {

    companion object {
        val TAG = WordPaginationRemoteMediator::class.simpleName
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.SKIP_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, BookmarkedWordCE>
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.APPEND -> {

                    val lastItem = state.lastItemOrNull()
                        ?: return MediatorResult.Success(endOfPaginationReached = true)

                    lastItem.date

                }
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            }


            val endCalendar = if (loadKey == null) {
                Calendar.getInstance(Locale.US)
            } else {
                CalenderUtil.convertStringToCalender(
                    loadKey,
                    CalenderUtil.DATE_FORMAT
                )!!
            }
            if (loadType == LoadType.APPEND) {
                //subtract the calendar by one day which will be the next startFrom
                endCalendar.roll(
                    Calendar.DATE, false
                )

            }
            val startFrom = CalenderUtil.convertCalenderToString(
                endCalendar,
                CalenderUtil.DATE_FORMAT
            )

            Log.i(TAG, "NEXT LOAD KEY: $startFrom")

            val response = safeApiCall(Dispatchers.IO) {
                wordNetworkDataSource.getWords(
                    startFrom = startFrom,
                    limit = NETWORK_PAGE_SIZE
                )
            }

            val resource =
                object : ApiResponseHandler<ApiResponse<List<Word>>, List<Word>>(response) {
                    override suspend fun handleSuccess(resultObj: ApiResponse<List<Word>>): Resource<List<Word>?> {
                        return if (resultObj.code == "200") {
                            Resource.success(resultObj.data)
                        } else {
                            Resource.error(Throwable(resultObj.message), null)
                        }
                    }
                }.getResult()


            if (resource.status == Status.SUCCESS) {
                if (loadType == LoadType.REFRESH) {
                    Log.i(TAG, "LOAD TYPE: REFRESH -- DELETE ALL WORDS")
                    wordCacheDataSource.deleteAll()
                }

                resource.data?.let {
                    wordCacheDataSource.addAll(it)
                }

                MediatorResult.Success(
                    endOfPaginationReached = (resource.data?.size ?: 0) < state.config.pageSize
                )
            } else {
                MediatorResult.Error(Exception(resource.error?.message))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.i(TAG, "ERROR: $e")
            MediatorResult.Error(e)
        }
    }


}
