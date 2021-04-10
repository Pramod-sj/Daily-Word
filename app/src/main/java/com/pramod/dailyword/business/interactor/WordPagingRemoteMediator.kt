package com.pramod.dailyword.business.interactor

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.pramod.dailyword.business.data.cache.abstraction.WordCacheDataSource
import com.pramod.dailyword.business.data.network.abstraction.WordNetworkDataSource
import com.pramod.dailyword.business.data.network.utils.handleApiException
import com.pramod.dailyword.framework.datasource.cache.model.BookmarkedWordCE
import com.pramod.dailyword.framework.prefmanagers.RemoteKeyPrefManager
import com.pramod.dailyword.framework.ui.words.PAGE_SIZE
import com.pramod.dailyword.framework.util.CalenderUtil
import retrofit2.HttpException
import java.util.*
import javax.inject.Inject

@ExperimentalPagingApi
class WordPaginationRemoteMediator @Inject constructor(
    private val wordNetworkDataSource: WordNetworkDataSource,
    private val wordCacheDataSource: WordCacheDataSource,
    private val remoteKeyPrefManager: RemoteKeyPrefManager
) : RemoteMediator<Int, BookmarkedWordCE>() {

    companion object {
        val TAG = WordPaginationRemoteMediator::class.simpleName
    }

    override suspend fun initialize(): InitializeAction {
        //only launch initial refresh when data in local is less then PAGE_SIZE
        return if ((wordCacheDataSource.getAll()?.size ?: 0) < PAGE_SIZE
        ) InitializeAction.LAUNCH_INITIAL_REFRESH
        else InitializeAction.SKIP_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, BookmarkedWordCE>
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.APPEND -> {

                    if (remoteKeyPrefManager.isReachedToEnd()) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }

                    if (remoteKeyPrefManager.getNextRemoteKey() == null) {
                        state.lastItemOrNull()?.let { last ->
                            CalenderUtil.convertStringToCalender(
                                last.date,
                                CalenderUtil.DATE_FORMAT
                            )?.also { calendar ->
                                calendar.roll(Calendar.DATE, false)
                                val nextKey =
                                    CalenderUtil.convertCalenderToString(
                                        calendar,
                                        CalenderUtil.DATE_FORMAT
                                    )
                                remoteKeyPrefManager.setNextRemoteKey(nextKey)
                            }
                        }
                    }

                    remoteKeyPrefManager.getNextRemoteKey()
                }
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            }


            val cal = if (loadKey == null) {
                Calendar.getInstance(Locale.US)
            } else {
                CalenderUtil.convertStringToCalender(
                    loadKey,
                    CalenderUtil.DATE_FORMAT
                )!!
            }

            val startFrom = CalenderUtil.convertCalenderToString(
                cal,
                CalenderUtil.DATE_FORMAT
            )

            Log.i(TAG, "NEXT LOAD KEY: $startFrom")

            val resource = wordNetworkDataSource.getWords(
                startFrom = startFrom,
                limit = state.config.pageSize
            )

            if (resource.code == "200") {
                Log.i(TAG, "load: success")

                if (loadType == LoadType.REFRESH) {
                    Log.i(TAG, "LOAD TYPE: REFRESH -- DELETE ALL WORDS")
                    wordCacheDataSource.deleteAll()
                    remoteKeyPrefManager.setReachedToEnd(false)
                    remoteKeyPrefManager.setNextRemoteKey(null)
                }


                resource.data?.let {

                    //subtract the calendar by one day which will be the next startFrom

                    CalenderUtil.convertStringToCalender(
                        it.last().date,
                        CalenderUtil.DATE_FORMAT
                    )?.let { calendar ->
                        calendar.roll(Calendar.DATE, false)
                        val nextKey =
                            CalenderUtil.convertCalenderToString(calendar, CalenderUtil.DATE_FORMAT)
                        remoteKeyPrefManager.setNextRemoteKey(nextKey)
                    }
                    wordCacheDataSource.addAll(it)
                }

                val isReachedToEnd = (resource.data?.size ?: 0) < state.config.pageSize

                remoteKeyPrefManager.setReachedToEnd(isReachedToEnd)

                MediatorResult.Success(
                    endOfPaginationReached = isReachedToEnd
                )
            } else {
                MediatorResult.Error(Exception(resource.message))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            MediatorResult.Error(handleApiException(e))
        }
    }


}

private fun convertErrorBody(throwable: HttpException): String? {
    return try {
        throwable.toString()
    } catch (exception: Exception) {
        "Unknown"
    }
}
