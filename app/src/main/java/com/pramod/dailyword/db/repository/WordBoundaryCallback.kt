package com.pramod.dailyword.db.repository

import android.util.Log
import androidx.paging.PagedList
import com.google.gson.Gson
import com.pramod.dailyword.db.Resource
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.helper.PagingRequestHelper
import com.pramod.dailyword.helper.createStatusLiveData
import com.pramod.dailyword.ui.words.NETWORK_PAGE_SIZE
import com.pramod.dailyword.util.CalenderUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.Executor

class WordBoundaryCallback(
    private val repository: WOTDRepository,
    private val executor: Executor
) : PagedList.BoundaryCallback<WordOfTheDay>() {
    private val TAG = "WordBoundaryCallback"

    val paginationHelper = PagingRequestHelper(executor)
    val networkState = paginationHelper.createStatusLiveData()
    var isReachedToEnd: Boolean = false

    override fun onZeroItemsLoaded() {
        paginationHelper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            GlobalScope.launch(Dispatchers.Main) {
                fetchWords(null, it)
            }
        }
    }

    override fun onItemAtEndLoaded(itemAtEnd: WordOfTheDay) {
        Log.i(TAG, itemAtEnd.date)
        if (!isReachedToEnd) {
            paginationHelper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
                GlobalScope.launch(Dispatchers.Main) {
                    fetchWords(findStartFrom(itemAtEnd.date!!), it)
                }
            }
        }
    }


    private suspend fun fetchWords(
        startFrom: String?,
        requestCallback: PagingRequestHelper.Request.Callback
    ) {
        val apiResponse = repository.getWords(startFrom, NETWORK_PAGE_SIZE)
        if (apiResponse.status == Resource.Status.SUCCESS) {
            apiResponse.data?.also {
                repository.addAllWord(it)
            }
            Log.i("RESPONSE $startFrom", Gson().toJson(apiResponse.data));
            isReachedToEnd = apiResponse.data.isNullOrEmpty()
            requestCallback.recordSuccess()
        } else {
            requestCallback.recordFailure(Throwable(apiResponse.message))
        }
    }


    private fun findStartFrom(date: String): String {
        Log.i("START FROM", date)
        val actualStartFrom = CalenderUtil.subtractDaysFromCalendar(
            date,
            1
        )
        Log.i("ACTUAL START FROM", actualStartFrom)
        return actualStartFrom
    }

}