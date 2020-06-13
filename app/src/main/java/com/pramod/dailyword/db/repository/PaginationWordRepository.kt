package com.pramod.dailyword.db.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.pramod.dailyword.db.local.AppDB
import com.pramod.dailyword.db.model.ApiResponse
import com.pramod.dailyword.db.model.Listing
import com.pramod.dailyword.db.model.NetworkState
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.util.CalenderUtil
import com.pramod.dailyword.util.CommonUtils
import com.pramod.dailyword.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class PaginationWordRepository(
    private val application: Application,
    private val pageSize: Int = 5,
    private val executor: Executor
) {
    private val apiService = NetworkUtils.getWOTDApiService()
    private val appDB = AppDB.getInstance(application)

    private fun insertWordsIntoDb(
        words: List<WordOfTheDay>
    ) {
        appDB!!.runInTransaction {
            GlobalScope.launch(Dispatchers.IO) {
                if (words.isNotEmpty()) {
                    for (i: WordOfTheDay in words) {
                        i.date?.let { date ->
                            val cal = CalenderUtil.convertStringToCalender(
                                date,
                                CalenderUtil.DATE_FORMAT
                            )
                            i.dateTimeInMillis = cal?.timeInMillis

                            val dayColor = CommonUtils.getColorBasedOnDay(cal)
                            i.wordColor = dayColor[0]
                            i.wordDesaturatedColor = dayColor[1]
                        }
                    }
                    appDB.getWordOfTheDayDao().addAll(words)
                }
            }

        }
    }


    private fun refreshAllWord(): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        apiService.getWords(limit = pageSize)
            .enqueue(object : Callback<ApiResponse<List<WordOfTheDay>>> {
                override fun onFailure(call: Call<ApiResponse<List<WordOfTheDay>>>, t: Throwable) {
                    when (t) {
                        is UnknownHostException -> {
                            networkState.value =
                                NetworkState.error("You don't have a proper internet connection")
                        }
                        is SocketTimeoutException -> {
                            networkState.value =
                                NetworkState.error("Timeout! Please check your internet connection or retry!")
                        }
                        else -> {
                            networkState.value = NetworkState.error(t.message)
                        }
                    }
                }

                override fun onResponse(
                    call: Call<ApiResponse<List<WordOfTheDay>>>,
                    response: Response<ApiResponse<List<WordOfTheDay>>>
                ) {
                    val apiResponse = response.body()
                    if (apiResponse != null) {
                        if (apiResponse.code == "200") {
                            executor.execute {
                                appDB!!.runInTransaction {
                                    GlobalScope.launch(Dispatchers.IO) {
                                        appDB.getWordOfTheDayDao().deleteAll()
                                        if (apiResponse.data != null) {
                                            insertWordsIntoDb(apiResponse.data!!)
                                        }
                                    }
                                }
                                networkState.postValue(NetworkState.LOADED)
                            }
                        } else {
                            networkState.value = NetworkState.error(apiResponse.message)
                        }
                    } else {
                        networkState.value = NetworkState.error("No response from server")
                    }
                }

            })
        return networkState
    }

    fun getAllWords(): Listing<WordOfTheDay> {
        val pagedListConfig = PagedList.Config.Builder()
            .setPageSize(pageSize)
            .setEnablePlaceholders(true)
            .build()
        val boundaryCallback = WordBoundaryCallback(
            apiService, appDB!!, pageSize, Executors.newSingleThreadExecutor()
        )

        val refreshTrigger = MutableLiveData<Unit?>()

        val refreshState = Transformations.switchMap(refreshTrigger) {
            it?.let {
                refreshAllWord()
            }
        }

        val livePagedList = LivePagedListBuilder(
            appDB.getWordOfTheDayDao().getAll()
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
            },
            refreshState = refreshState
        )
    }

}