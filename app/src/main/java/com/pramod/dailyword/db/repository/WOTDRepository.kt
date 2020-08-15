package com.pramod.dailyword.db.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.*
import com.pramod.dailyword.db.NetworkBoundedResource
import com.pramod.dailyword.db.Resource
import com.pramod.dailyword.db.local.AppDB
import com.pramod.dailyword.db.model.ApiResponse
import com.pramod.dailyword.db.model.Listing
import com.pramod.dailyword.db.model.NetworkState
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.db.remote.*
import com.pramod.dailyword.ui.words.LOCAL_PAGE_SIZE
import com.pramod.dailyword.ui.words.NETWORK_PAGE_SIZE
import com.pramod.dailyword.util.CalenderUtil
import com.pramod.dailyword.util.CommonUtils
import com.pramod.dailyword.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class WOTDRepository(private val context: Context) {
    val localDb: AppDB = AppDB.getInstance(context)
    val remoteApiService: WOTDApiService = NetworkUtils.getWOTDApiService()

    fun getTodaysWordOfTheDay(): LiveData<Resource<WordOfTheDay?>> {
        return object : NetworkBoundedResource<WordOfTheDay, WordOfTheDay>() {
            override fun loadLocalData(): LiveData<WordOfTheDay?> {
                return localDb.getWordOfTheDayDao().getJustTopOne()
            }

            override fun shouldFetch(data: WordOfTheDay?): Boolean {
                return data == null || data.date != CalenderUtil.convertCalenderToString(
                    Calendar.getInstance(),
                    CalenderUtil.DATE_FORMAT
                )
            }

            override suspend fun callRequest(): ApiResponse<WordOfTheDay?>? {
                return remoteApiService.getWordOfTheDay()
            }

            override suspend fun saveCallResult(item: WordOfTheDay?) {
                item?.let {
                    it.date?.let { date ->
                        val cal =
                            CalenderUtil.convertStringToCalender(date, CalenderUtil.DATE_FORMAT)
                        it.dateTimeInMillis =
                            cal?.timeInMillis
                        val dayColor = CommonUtils.getColorBasedOnDay(cal)
                        it.wordColor = dayColor[0]
                        it.wordDesaturatedColor = dayColor[1]
                        localDb.getWordOfTheDayDao().add(it)
                    }
                }
            }
        }.asLiveData()
    }

    fun getWordOfTheDayExceptTopOne(count: Int): LiveData<Resource<List<WordOfTheDay>?>> {
        var lastWotdDate: WordOfTheDay? = null
        return object : NetworkBoundedResource<List<WordOfTheDay>, List<WordOfTheDay>>() {
            override fun loadLocalData(): LiveData<List<WordOfTheDay>?> {
                return localDb.getWordOfTheDayDao().getFewExceptTopOne(count)
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

            override suspend fun callRequest(): ApiResponse<List<WordOfTheDay>?>? {
                val calendar: Calendar = if (lastWotdDate != null) {
                    CalenderUtil.convertStringToCalender(
                        lastWotdDate!!.date!!,
                        CalenderUtil.DATE_FORMAT
                    )!!
                } else {
                    Calendar.getInstance()
                }
                calendar.add(Calendar.DATE, -1)
                val startFrom = CalenderUtil.convertCalenderToString(
                    calendar,
                    CalenderUtil.DATE_FORMAT
                )
                return remoteApiService.getWords(startFrom, count)
            }

            override suspend fun saveCallResult(items: List<WordOfTheDay>?) {
                items?.let {
                    addAllWord(it)
                }
            }
        }.asLiveData()
    }


    fun getWords(count: Int = 7): LiveData<Resource<List<WordOfTheDay>?>> {
        return object : NetworkBoundedResource<List<WordOfTheDay>, List<WordOfTheDay>>() {
            override fun loadLocalData(): LiveData<List<WordOfTheDay>?> {
                return localDb.getWordOfTheDayDao().getFewFromTop(count)
            }

            override fun shouldFetch(data: List<WordOfTheDay>?): Boolean {
                return data == null || data.isEmpty() || (!data.isEmpty() && data[0].date != CalenderUtil.convertCalenderToString(
                    Calendar.getInstance(),
                    CalenderUtil.DATE_FORMAT
                ))
            }

            override suspend fun callRequest(): ApiResponse<List<WordOfTheDay>?>? {
                return remoteApiService.getWords(limit = count)
            }

            override suspend fun saveCallResult(items: List<WordOfTheDay>?) {
                items?.let {
                    addAllWord(it)
                }
            }
        }.asLiveData()
    }


    /*@ExperimentalPagingApi
    fun getAllWords(pageSize: Int): Flow<PagingData<WordOfTheDay>> {
        return Pager(
            config = PagingConfig(pageSize = pageSize),
            remoteMediator = WordPaginationRemoteMediator(this)
        ) {
            localDb.getWordOfTheDayDao().pagingSourceWords()
        }.flow
    }*/


    suspend fun getWords(startFrom: String?, limit: Int): Resource<List<WordOfTheDay>?> {
        return try {
            val apiResponse: ApiResponse<List<WordOfTheDay>?>? =
                remoteApiService.getWords(startFrom, limit)
            if (apiResponse != null) {
                if (apiResponse.code == "200") {
                    handleApiSuccess(apiResponse.data)
                } else {
                    handleApiFailure<List<WordOfTheDay>>(null, apiResponse.message)
                }
            } else {
                handleApiFailure(null, "No response from server")
            }
        } catch (e: Exception) {
            handleNetworkException(null, e)
        }
    }


    private fun refreshAllWord(): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        GlobalScope.launch(Dispatchers.Main) {
            val apiResponse = getWords(null, NETWORK_PAGE_SIZE)
            if (apiResponse.status == Resource.Status.SUCCESS) {
                deleteAllWords()
                apiResponse.data?.also {
                    addAllWord(it)
                }
                networkState.value = NetworkState.LOADED
            } else {
                networkState.value = NetworkState.error(apiResponse.message)
            }
        }
        return networkState
    }

    fun getAllWords(): Listing<WordOfTheDay> {
        val pagedListConfig = PagedList.Config.Builder()
            .setPageSize(LOCAL_PAGE_SIZE)
            .setEnablePlaceholders(false)
            .build()
        val boundaryCallback = WordBoundaryCallback(
            this, Executors.newSingleThreadExecutor()
        )

        val refreshTrigger = MutableLiveData<Unit>()

        val refreshState = Transformations.switchMap(refreshTrigger) {
            refreshAllWord()
        }

        val livePagedList = LivePagedListBuilder(
            localDb.getWordOfTheDayDao().dataSourceWords()
            , pagedListConfig
        ).setBoundaryCallback(boundaryCallback).build()
        return Listing(
            pagedList = livePagedList,
            networkState = boundaryCallback.networkState,
            retry = {
                boundaryCallback.paginationHelper.retryAllFailed()
            },
            refresh = {
                refreshTrigger.value = null
                boundaryCallback.isReachedToEnd = false
            },
            refreshState = refreshState
        )
    }

    fun getWord(word: String): LiveData<WordOfTheDay> =
        localDb.getWordOfTheDayDao().getWord(word)

    suspend fun getJustTopOneNonLive(): WordOfTheDay? =
        localDb.getWordOfTheDayDao().getJustTopOneNonLive()

    suspend fun getJustNonLive(dateString: String): WordOfTheDay? =
        localDb.getWordOfTheDayDao().getJustNonLive(dateString)

    suspend fun refillWords(words: List<WordOfTheDay>) {
        deleteAllWords()
        addAllWord(words)
    }

    suspend fun addWord(word: WordOfTheDay): Long = localDb.getWordOfTheDayDao().add(word)

    suspend fun addAllWord(words: List<WordOfTheDay>) {
        for (i: WordOfTheDay in words) {
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
        localDb.getWordOfTheDayDao().addAll(words)
    }

    suspend fun deleteAllWords() = localDb.getWordOfTheDayDao().deleteAll()

    suspend fun updateWord(word: WordOfTheDay): Int = localDb.getWordOfTheDayDao().update(word)

}