package com.pramod.dailyword.db.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.pramod.dailyword.db.Resource
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.ui.words.NETWORK_PAGE_SIZE
import com.pramod.dailyword.util.CalenderUtil
import java.util.*

@ExperimentalPagingApi
class WordPaginationRemoteMediator(
    private val wordRepository: WOTDRepository
) : RemoteMediator<Int, WordOfTheDay>() {

    companion object {
        val TAG = WordPaginationRemoteMediator::class.simpleName
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.SKIP_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, WordOfTheDay>
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

            val resource = wordRepository.getWords(
                startFrom = startFrom,
                limit = NETWORK_PAGE_SIZE
            )

            Log.i(TAG, "API RESPONSE: COUNT:${resource.data?.size ?: 0}")

            if (resource.status == Resource.Status.SUCCESS) {
                wordRepository.localDb.withTransaction {
                    if (loadType == LoadType.REFRESH) {
                        Log.i(TAG, "LOAD TYPE: REFRESH -- DELETE ALL WORDS")
                        wordRepository.localDb.getWordOfTheDayDao().deleteAll()
                    }

                    resource.data?.let {
                        wordRepository.addAllWord(it)
                    }
                }
                MediatorResult.Success(
                    endOfPaginationReached = (resource.data?.size ?: 0) < state.config.pageSize
                )
            } else {
                MediatorResult.Error(Exception(resource.message))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.i(TAG, "ERROR: $e")
            MediatorResult.Error(e)
        }
    }


}
