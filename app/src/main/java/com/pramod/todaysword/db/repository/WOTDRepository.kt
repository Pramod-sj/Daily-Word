package com.pramod.todaysword.db.repository

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.work.*
import com.google.gson.Gson
import com.pramod.todaysword.db.NetworkBoundedResource
import com.pramod.todaysword.db.Resource
import com.pramod.todaysword.db.local.AppDB
import com.pramod.todaysword.db.model.ApiResponse
import com.pramod.todaysword.db.model.Listing
import com.pramod.todaysword.db.model.NetworkState
import com.pramod.todaysword.db.model.WordOfTheDay
import com.pramod.todaysword.db.remote.WOTDApiService
import com.pramod.todaysword.helper.PagingRequestHelper
import com.pramod.todaysword.util.CalenderUtil
import com.pramod.todaysword.util.CommonUtils
import com.pramod.todaysword.util.NetworkUtils
import com.pramod.todaysword.worker.DailyWordWorker
import com.pramod.todaysword.worker.NewWordReminderWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.coroutines.suspendCoroutine

class WOTDRepository(private val context: Context) {
    private var localDb: AppDB? = null;
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
                item?.let {
                    it.date?.let { date ->
                        it.dateTimeInMillis =
                            CalenderUtil.convertStringToCalender(date, CalenderUtil.DATE_FORMAT)
                                ?.timeInMillis
                        localDb!!.getWordOfTheDayDao().add(it)
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
                val calendar: Calendar
                calendar = if (lastWotdDate != null) {
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
                return remoteApiService.getWords(startFrom, count);
            }

            override fun saveCallResult(items: List<WordOfTheDay>?) {
                items?.let {
                    for (i: WordOfTheDay in items) {
                        i.date?.let { date ->
                            i.dateTimeInMillis =
                                CalenderUtil.convertStringToCalender(date, CalenderUtil.DATE_FORMAT)
                                    ?.timeInMillis

                        }
                    }
                    localDb!!.getWordOfTheDayDao().addAll(it)
                }
            }
        }.asLiveData()
    }


    suspend fun getJustNonLive(dateString: String): WordOfTheDay? =
        withContext(Dispatchers.IO) {
            return@withContext localDb!!.getWordOfTheDayDao().getJustNonLive(dateString)
        }

    suspend fun addWord(word: WordOfTheDay): Long = withContext(Dispatchers.IO) {
        return@withContext localDb!!.getWordOfTheDayDao().add(word)
    }

    suspend fun updateWord(word: WordOfTheDay): Int = withContext(Dispatchers.IO) {
        return@withContext localDb!!.getWordOfTheDayDao().update(word)
    }
}