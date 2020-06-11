package com.pramod.todaysword.db.repository

import android.util.Log
import androidx.paging.PagedList
import com.google.gson.Gson
import com.pramod.todaysword.db.Resource
import com.pramod.todaysword.db.local.AppDB
import com.pramod.todaysword.db.model.ApiResponse
import com.pramod.todaysword.db.model.WordOfTheDay
import com.pramod.todaysword.db.remote.WOTDApiService
import com.pramod.todaysword.helper.PagingRequestHelper
import com.pramod.todaysword.helper.createStatusLiveData
import com.pramod.todaysword.util.CalenderUtil
import com.pramod.todaysword.util.CommonUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.Executor

class WordBoundaryCallback(
    private val apiService: WOTDApiService,
    private val localDb: AppDB,
    private val pageSize: Int,
    private val executor: Executor
) : PagedList.BoundaryCallback<WordOfTheDay>() {
    private val TAG = "WordBoundaryCallback"

    val paginationHelper = PagingRequestHelper(executor)
    val networkState = paginationHelper.createStatusLiveData()

    override fun onZeroItemsLoaded() {
        super.onZeroItemsLoaded()
        paginationHelper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            apiService.getWords(
                limit = pageSize
            ).enqueue(createWebServiceCallback(it))
        }
    }

    override fun onItemAtEndLoaded(itemAtEnd: WordOfTheDay) {
        super.onItemAtEndLoaded(itemAtEnd)
        Log.i(TAG, itemAtEnd.date)
        paginationHelper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {

            val endCalendar = CalenderUtil.convertStringToCalender(
                itemAtEnd.date!!,
                CalenderUtil.DATE_FORMAT
            )
            //subtract the calendar by one day which will be the next startFrom
            endCalendar!!.roll(Calendar.DATE, false)
            val startFrom = CalenderUtil.convertCalenderToString(
                endCalendar,
                CalenderUtil.DATE_FORMAT
            )

            apiService.getWords(
                startFrom = startFrom,
                limit = pageSize
            ).enqueue(createWebServiceCallback(it))
        }
    }


    private fun createWebServiceCallback(it: PagingRequestHelper.Request.Callback):
            Callback<ApiResponse<List<WordOfTheDay>>> {
        return object : Callback<ApiResponse<List<WordOfTheDay>>> {
            override fun onFailure(call: Call<ApiResponse<List<WordOfTheDay>>>, t: Throwable) {
                if (t is UnknownHostException) {
                    it.recordFailure(Throwable("You don't have a proper internet connection"))
                } else if (t is SocketTimeoutException) {
                    it.recordFailure(Throwable("Timeout! Please check your internet connection or retry!"))
                } else {
                    it.recordFailure(t)
                }
            }

            override fun onResponse(
                call: Call<ApiResponse<List<WordOfTheDay>>>,
                response: Response<ApiResponse<List<WordOfTheDay>>>
            ) {
                insertItemIntoDb(response, it)
            }

        }
    }

    private fun insertItemIntoDb(
        response: Response<ApiResponse<List<WordOfTheDay>>>,
        it: PagingRequestHelper.Request.Callback
    ) {
        executor.execute {
            val apiResponse = response.body()
            if (apiResponse != null) {
                if (apiResponse.code == "200") {
                    apiResponse.data?.let {
                        if (it.isNotEmpty()) {
                            Log.i(TAG, "RESPONSE DATA LIST COUNT: ${it.size}")
                            for (i: WordOfTheDay in apiResponse.data!!) {
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
                            Log.i(TAG, Gson().toJson(it))
                            localDb.getWordOfTheDayDao().addAll(it)
                        }
                    }
                    it.recordSuccess()
                } else {
                    it.recordFailure(Throwable(apiResponse.message))
                }
            } else {
                it.recordFailure(Throwable("No response from server"))
            }
        }
    }


}