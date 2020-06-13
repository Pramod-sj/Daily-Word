package com.pramod.dailyword.db.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.pramod.dailyword.db.NetworkBoundedResource
import com.pramod.dailyword.db.Resource
import com.pramod.dailyword.db.local.AppDB
import com.pramod.dailyword.db.model.ApiResponse
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.db.remote.WOTDApiService
import com.pramod.dailyword.util.CalenderUtil
import com.pramod.dailyword.util.CommonUtils
import com.pramod.dailyword.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import java.util.*

class WOTDRepository(private val context: Context) {
    private var localDb: AppDB? = null
    val remoteApiService: WOTDApiService = NetworkUtils.getWOTDApiService()

    init {
        localDb = AppDB.getInstance(context)
    }

    fun getTodaysWordOfTheDay(): LiveData<Resource<WordOfTheDay?>> {
        return object : NetworkBoundedResource<WordOfTheDay, WordOfTheDay>() {
            override fun loadLocalDb(): LiveData<WordOfTheDay?> {
                return localDb!!.getWordOfTheDayDao().getJustTopOne()
            }

            override fun shouldFetch(data: WordOfTheDay?): Boolean {
                return data == null || data.date != CalenderUtil.convertCalenderToString(
                    Calendar.getInstance(),
                    CalenderUtil.DATE_FORMAT
                )
            }

            override fun callRequest(): Call<ApiResponse<WordOfTheDay>> {
                return remoteApiService.getWordOfTheDay()
            }

            override fun saveCallResult(item: WordOfTheDay?) {
                GlobalScope.launch(Dispatchers.IO) {
                    item?.let {
                        it.date?.let { date ->
                            val cal =
                                CalenderUtil.convertStringToCalender(date, CalenderUtil.DATE_FORMAT)
                            it.dateTimeInMillis =
                                cal?.timeInMillis
                            val dayColor = CommonUtils.getColorBasedOnDay(cal)
                            it.wordColor = dayColor[0]
                            it.wordDesaturatedColor = dayColor[1]
                            localDb!!.getWordOfTheDayDao().add(it)
                        }
                    }
                }

            }
        }.asLiveData()
    }

    fun getWordOfTheDayExceptTopOne(count: Int): LiveData<Resource<List<WordOfTheDay>?>> {
        var lastWotdDate: WordOfTheDay? = null
        return object : NetworkBoundedResource<List<WordOfTheDay>, List<WordOfTheDay>>() {
            override fun loadLocalDb(): LiveData<List<WordOfTheDay>?> {
                return localDb!!.getWordOfTheDayDao().getFewExceptTopOne(count)
            }

            override fun shouldFetch(data: List<WordOfTheDay>?): Boolean {
                data?.let {
                    if (data.isNotEmpty()) {
                        lastWotdDate = data[0]
                    }
                }

                return data == null || data.isEmpty() || data[0].date != CalenderUtil.convertCalenderToString(
                    Calendar.getInstance(),
                    CalenderUtil.DATE_FORMAT
                )
            }

            override fun callRequest(): Call<ApiResponse<List<WordOfTheDay>>> {
                val calendar: Calendar = if (lastWotdDate != null) {
                    CalenderUtil.convertStringToCalender(
                        lastWotdDate!!.date!!,
                        CalenderUtil.DATE_FORMAT
                    )!!
                } else {
                    Calendar.getInstance()
                }
                calendar.roll(Calendar.DATE, false)
                val startFrom = CalenderUtil.convertCalenderToString(
                    calendar,
                    CalenderUtil.DATE_FORMAT
                )
                return remoteApiService.getWords(startFrom, count)
            }

            override fun saveCallResult(items: List<WordOfTheDay>?) {
                GlobalScope.launch(Dispatchers.IO) {
                    items?.let {
                        for (i: WordOfTheDay in items) {
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
                        localDb!!.getWordOfTheDayDao().addAll(it)
                    }
                }
            }
        }.asLiveData()
    }

    suspend fun getJustTopOneNonLive(): WordOfTheDay? =
        localDb!!.getWordOfTheDayDao().getJustTopOneNonLive()

    suspend fun getJustNonLive(dateString: String): WordOfTheDay? =
        localDb!!.getWordOfTheDayDao().getJustNonLive(dateString)

    suspend fun addWord(word: WordOfTheDay): Long = localDb!!.getWordOfTheDayDao().add(word)

    suspend fun updateWord(word: WordOfTheDay): Int = localDb!!.getWordOfTheDayDao().update(word)

}