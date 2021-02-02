package com.pramod.dailyword.db.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.asLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.google.gson.Gson
import com.pramod.dailyword.db.NetworkBoundResource
import com.pramod.dailyword.db.Resource
import com.pramod.dailyword.db.local.AppDB
import com.pramod.dailyword.db.model.ApiResponse
import com.pramod.dailyword.db.model.Listing
import com.pramod.dailyword.db.model.NetworkState
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.db.remote.WOTDApiService
import com.pramod.dailyword.db.remote.handleApiFailure
import com.pramod.dailyword.db.remote.handleApiSuccess
import com.pramod.dailyword.db.remote.handleNetworkException
import com.pramod.dailyword.ui.words.LOCAL_PAGE_SIZE
import com.pramod.dailyword.ui.words.NETWORK_PAGE_SIZE
import com.pramod.dailyword.util.CalenderUtil
import com.pramod.dailyword.util.CommonUtils
import com.pramod.dailyword.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.Executors

class WOTDRepository(private val context: Context) {
    val localDb: AppDB = AppDB.getInstance(context)
    val remoteApiService: WOTDApiService = NetworkUtils.getWOTDApiService()

    fun getTodaysWordOfTheDay(): LiveData<Resource<WordOfTheDay?>> {

        return object : NetworkBoundResource<WordOfTheDay, WordOfTheDay>() {
            override suspend fun fetchFromCache(): Flow<WordOfTheDay?> {
                return localDb.getWordOfTheDayDao().getJustTopOne()
            }

            override fun shouldFetchFromNetwork(data: WordOfTheDay?): Boolean {
                return data == null || data.date != CalenderUtil.convertCalenderToString(
                    Calendar.getInstance(Locale.US),
                    CalenderUtil.DATE_FORMAT
                )
            }

            override suspend fun saveIntoCache(data: WordOfTheDay) {
                data.date?.let { date ->
                    val cal =
                        CalenderUtil.convertStringToCalender(date, CalenderUtil.DATE_FORMAT)
                    data.dateTimeInMillis =
                        cal?.timeInMillis
                    val dayColor = CommonUtils.getColorBasedOnDay(cal)
                    data.wordColor = dayColor[0]
                    data.wordDesaturatedColor = dayColor[1]
                    localDb.getWordOfTheDayDao().add(data)
                }
            }

            override suspend fun fetchFromNetwork(): ApiResponse<WordOfTheDay> {
                return remoteApiService.getWordOfTheDay()
            }

        }.asFlow().asLiveData(Dispatchers.IO)
    }

    fun getWords(count: Int = 7): LiveData<Resource<List<WordOfTheDay>?>> {
        return object : NetworkBoundResource<List<WordOfTheDay>, List<WordOfTheDay>>() {
            override suspend fun fetchFromCache(): Flow<List<WordOfTheDay>?> {
                return localDb.getWordOfTheDayDao().getFewFromTopAsFlow(count)
            }

            override fun shouldFetchFromNetwork(data: List<WordOfTheDay>?): Boolean {
                return true
            }

            override suspend fun saveIntoCache(data: List<WordOfTheDay>) {
                addAllWord(data)
            }

            override suspend fun fetchFromNetwork(): ApiResponse<List<WordOfTheDay>> {
                return remoteApiService.getWords(limit = count)
            }

        }.asFlow().asLiveData(Dispatchers.IO)
    }

    fun recapWords(count: Int = 7): LiveData<Resource<List<WordOfTheDay>?>> {
        return object : NetworkBoundResource<List<WordOfTheDay>, List<WordOfTheDay>>() {

            override suspend fun fetchFromCache(): Flow<List<WordOfTheDay>?> {

                val calendarList = arrayListOf<Calendar>()
                for (i in 0 downTo -6) {
                    val today = CalenderUtil.getCalendarInstance(true)
                    today.add(Calendar.DATE, i)
                    calendarList.add(today)

                }
                Log.i(TAG, "loadLocalData: ${calendarList.size}")
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
                    Log.i(
                        TAG,
                        "loadLocalData: ${
                            CalenderUtil.getDayName(
                                cal.timeInMillis
                            )
                        } ${
                            CalenderUtil.convertCalenderToString(
                                cal.timeInMillis,
                                CalenderUtil.DATE_TIME_FORMAT
                            )
                        }"
                    )

                }

                Log.i(
                    TAG,
                    "loadLocalData: ${
                        CalenderUtil.convertCalenderToString(
                            calendarsTillMonday.last(),
                            CalenderUtil.DATE_TIME_FORMAT
                        )
                    }"
                )

                Log.i(TAG, "loadLocalData: last ${calendarsTillMonday.last()}")
                return localDb.getWordOfTheDayDao()
                    .getFewTillAsFlow(
                        fromDate = calendarsTillMonday.first(),
                        tillDate = calendarsTillMonday.last(),
                        count = count
                    )
            }

            override fun shouldFetchFromNetwork(data: List<WordOfTheDay>?): Boolean = true


            override suspend fun saveIntoCache(data: List<WordOfTheDay>) {
                addAllWord(data)
            }

            override suspend fun fetchFromNetwork(): ApiResponse<List<WordOfTheDay>> {
                return remoteApiService.getWords(limit = count)
            }
        }.asFlow().asLiveData(Dispatchers.IO)
    }

    fun getWord(date: String, forceRefresh: Boolean = false): LiveData<Resource<WordOfTheDay?>> {
        return object : NetworkBoundResource<List<WordOfTheDay>, WordOfTheDay>() {

            override suspend fun fetchFromCache(): Flow<WordOfTheDay?> =
                localDb.getWordOfTheDayDao().getJustAsFlow(date)

            override fun shouldFetchFromNetwork(data: WordOfTheDay?): Boolean = forceRefresh

            override suspend fun saveIntoCache(data: List<WordOfTheDay>) {
                data.firstOrNull()?.let {
                    it.date?.let { date ->
                        val cal =
                            CalenderUtil.convertStringToCalender(date, CalenderUtil.DATE_FORMAT)
                        it.dateTimeInMillis = cal?.timeInMillis
                        val dayColor = CommonUtils.getColorBasedOnDay(cal)
                        it.wordColor = dayColor[0]
                        it.wordDesaturatedColor = dayColor[1]
                        localDb.getWordOfTheDayDao().add(it)
                    }
                }
            }

            override suspend fun fetchFromNetwork(): ApiResponse<List<WordOfTheDay>> {
                return remoteApiService.getWords(date, 1)
            }

        }.asFlow().asLiveData(Dispatchers.IO)
    }

    companion object {
        val TAG = WOTDRepository::class.java.simpleName
    }

    fun getRandomWord(): LiveData<Resource<WordOfTheDay?>> {
        return flow<Resource<WordOfTheDay?>> {
            try {
                emit(Resource.loading(null))
                val apiResponse = remoteApiService.getRandomWord()
                Log.i(TAG, "getRandomWord: ${Gson().toJson(apiResponse)}")
                if (apiResponse != null) {
                    if (apiResponse.code == "200") {
                        val word = apiResponse.data
                        if (word != null) {
                            word.date?.let { date ->
                                val cal =
                                    CalenderUtil.convertStringToCalender(
                                        date,
                                        CalenderUtil.DATE_FORMAT
                                    )
                                word.dateTimeInMillis = cal?.timeInMillis
                                val dayColor = CommonUtils.getColorBasedOnDay(cal)
                                word.wordColor = dayColor[0]
                                word.wordDesaturatedColor = dayColor[1]
                                localDb.getWordOfTheDayDao().add(word)
                            }

                            emitAll(
                                localDb.getWordOfTheDayDao().getWordFlow(word.word!!)
                                    .map {
                                        handleApiSuccess(it)
                                    }
                            )
                        } else {
                            emit(
                                handleApiFailure<WordOfTheDay?>(null, "No word found")
                            )
                        }

                    } else {
                        emit(
                            handleApiFailure<WordOfTheDay?>(null, apiResponse.message)
                        )
                    }
                } else {
                    emit(
                        handleApiFailure<WordOfTheDay?>(null, "No response from server")
                    )
                }
            } catch (e: java.lang.Exception) {
                emit(handleNetworkException<WordOfTheDay?>(null, e))
            }
        }.asLiveData(Dispatchers.IO)
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
            val apiResponse: ApiResponse<List<WordOfTheDay>> =
                remoteApiService.getWords(startFrom, limit)
            if (apiResponse.code == "200") {
                handleApiSuccess(apiResponse.data)
            } else {
                handleApiFailure(null, apiResponse.message)
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
            this,
            Executors.newSingleThreadExecutor()
        )

        val refreshTrigger = MutableLiveData<Unit>()

        val refreshState = Transformations.switchMap(refreshTrigger) {
            refreshAllWord()
        }

        val livePagedList = LivePagedListBuilder(
            localDb.getWordOfTheDayDao().dataSourceWords(),
            pagedListConfig
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

    fun getWordLocally(word: String): LiveData<WordOfTheDay> =
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
        Log.i("WOTDRepository", "addAllWord: ${Gson().toJson(words)}")
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