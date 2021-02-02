package com.pramod.dailyword.db.repository

import android.util.Log
/*import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator*/
import androidx.room.withTransaction
import com.google.gson.Gson
import com.pramod.dailyword.db.Resource
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.db.remote.NetworkResponse
import com.pramod.dailyword.ui.words.NETWORK_PAGE_SIZE
import com.pramod.dailyword.util.CalenderUtil
import com.pramod.dailyword.util.CommonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
/*

@ExperimentalPagingApi
class WordPaginationRemoteMediator(
    private val wordRepository: WOTDRepository
) : RemoteMediator<Int, WordOfTheDay>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, WordOfTheDay>
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.APPEND -> {
                    Log.i("ANCHOR POS", state.anchorPosition.toString())
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

            Log.i("NEXT LOAD KEY", startFrom)

            val resource = wordRepository.getWords(
                startFrom = startFrom,
                limit = NETWORK_PAGE_SIZE
            )

            Log.i("API RESPONSE", Gson().toJson(resource))

            if (resource.status == Resource.Status.SUCCESS) {
                wordRepository.localDb.withTransaction {
                    if (loadType == LoadType.REFRESH) {
                        Log.i("LOAD TYPE", "REFRESH -- DELETE ALL WORDS")
                        wordRepository.localDb.getWordOfTheDayDao().deleteAll()
                    }

                    if (resource.data != null) {
                        for (i: WordOfTheDay in resource.data) {
                            i.date?.let { date ->
                                val cal =
                                    CalenderUtil.convertStringToCalender(
                                        date,
                                        CalenderUtil.DATE_FORMAT
                                    )
                                i.dateTimeInMillis = cal?.timeInMillis
                                val dayColor = CommonUtils.getColorBasedOnDay(cal)
                                i.wordColor = dayColor[0]
                                i.wordDesaturatedColor = dayColor[1]
                            }
                        }
                        Log.i(
                            "INSERTED",
                            Gson().toJson(
                                wordRepository.localDb.getWordOfTheDayDao().addAll(resource.data)
                            )
                        )
                    }
                }
                MediatorResult.Success(endOfPaginationReached = resource.data.isNullOrEmpty())
            } else {
                MediatorResult.Error(Exception(resource.message))
            }

        } catch (e: Exception) {
            Log.i("ERROR", e.toString())
            MediatorResult.Error(e)
        }
    }


}*/
