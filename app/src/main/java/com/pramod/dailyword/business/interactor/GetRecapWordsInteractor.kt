package com.pramod.dailyword.business.interactor

import com.pramod.dailyword.business.data.cache.abstraction.BookmarkedWordCacheDataSource
import com.pramod.dailyword.business.data.cache.abstraction.WordCacheDataSource
import com.pramod.dailyword.business.data.network.NetworkBoundResource
import com.pramod.dailyword.business.data.network.Resource
import com.pramod.dailyword.business.data.network.abstraction.WordNetworkDataSource
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.framework.datasource.network.model.api.ApiResponse
import com.pramod.dailyword.framework.util.CalenderUtil
import kotlinx.coroutines.flow.Flow
import java.util.*

class GetRecapWordsInteractor constructor(
    private val bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource,
    private val wordCacheDataSource: WordCacheDataSource,
    private val wordNetworkDataSource: WordNetworkDataSource
) {
    fun getRecap(count: Int = 7): Flow<Resource<List<Word>?>> {
        return object : NetworkBoundResource<List<Word>, List<Word>>() {
            override suspend fun fetchFromCache(): Flow<List<Word>?> {
                val list = calculateFromDateAndTillDate()
                val from = list[0]
                val till = list[1]
                return bookmarkedWordCacheDataSource.getFewWordsTillAsFlow(
                    fromDate = from,
                    tillDate = till,
                    count = count
                )
            }

            override fun shouldFetchFromNetwork(data: List<Word>?): Boolean {
                return true
            }

            override suspend fun saveIntoCache(data: List<Word>) {
                wordCacheDataSource.addAll(data)
            }

            override suspend fun fetchFromNetwork(): ApiResponse<List<Word>> {
                return wordNetworkDataSource.getWords(null, count)
            }

        }.asFlow()
    }


    private fun calculateFromDateAndTillDate(): List<Long> {
        val calendarList = arrayListOf<Calendar>()
        for (i in 0 downTo -6) {
            val today = CalenderUtil.getCalendarInstance(true)
            today.add(Calendar.DATE, i)
            calendarList.add(today)

        }
        //item in this list will be sun(21-may)->sat(20-may)->friday(19-may)->thus(18-may) till mon
        val calendarsTillMonday = arrayListOf<Long>()

        for ((index, cal) in calendarList.withIndex()) {
            if (CalenderUtil.getDayNameBasedOnDayOfWeek(cal.get(Calendar.DAY_OF_WEEK)) ==
                CalenderUtil.DAYS[0]
            ) {
                //if the first item was sunday than don't break and iterate for all items
                if (index != 0) {
                    break
                }
            }

            calendarsTillMonday.add(cal.timeInMillis)

        }

        return listOf(
            calendarsTillMonday.first(),
            calendarsTillMonday.last(),
        )
    }
}

