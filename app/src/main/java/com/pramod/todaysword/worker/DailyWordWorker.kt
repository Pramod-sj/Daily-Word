package com.pramod.todaysword.worker

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.pramod.todaysword.db.local.AppDB
import com.pramod.todaysword.db.model.ApiResponse
import com.pramod.todaysword.db.model.ServerTime
import com.pramod.todaysword.db.model.WordOfTheDay
import com.pramod.todaysword.db.remote.TimeApiService
import com.pramod.todaysword.db.remote.WOTDApiService
import com.pramod.todaysword.db.repository.WOTDRepository
import com.pramod.todaysword.helper.NotificationHelper
import com.pramod.todaysword.util.CalenderUtil
import com.pramod.todaysword.util.NetworkUtils
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.coroutines.suspendCoroutine
import kotlin.Result as CoroutineResult

class DailyWordWorker(val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    private val repo: WOTDRepository = WOTDRepository(context)
    private val localDb: AppDB = AppDB.getInstance(context)!!
    private val apiService: WOTDApiService =
        NetworkUtils.getWOTDApiService()
    private val timeApiService: TimeApiService =
        NetworkUtils.getServerTimeApiService()
    private val notificationHelper: NotificationHelper = NotificationHelper(context)

    companion object {
        const val TAG = "DailyWordWorker"
    }

    override fun doWork(): Result {
        Handler(Looper.getMainLooper()).post {
            val workInfosLiveData =
                WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(TAG)
            val observer = object : Observer<List<WorkInfo>> {
                override fun onChanged(t: List<WorkInfo>) {
                    for (workInfo in t) {
                        Log.d(TAG, workInfo.state.toString());
                        if (workInfo.state.isFinished) {
                            //reschedule work when succeeded
                            Log.d(TAG, "RE SCHEDULE");
                            //WOTDRepository(context).initGetWordOfTheDayWorker();
                            Log.d(TAG, "Removing livedata observer");
                            workInfosLiveData.removeObserver(this)
                        }

                    }
                }
            }
            workInfosLiveData.observeForever(observer)

        }

        Log.d(TAG, "ATTEMPT COUNT: $runAttemptCount")
        return if (runAttemptCount > 5) {
            Log.d(TAG, "EXCEED ATTEMPT COUNT-->return result.failure()")
            Result.failure()
        } else {
            Log.d(TAG, "IN DO WORK")
            runBlocking {
                Log.d(TAG, "IN SIDE RUN BLOCKING")
                getTodaysWord()
            }
        }
    }

    suspend fun getTodaysWord(): Result = suspendCoroutine { continuation ->
        Log.d(TAG, "GETTING TIME FROM API")
        //get actual time from server
        var wordOfTheDay: WordOfTheDay?
        timeApiService.getTime().enqueue(object : Callback<ServerTime> {

            override fun onFailure(call: Call<ServerTime>, t: Throwable) {
                Log.d(TAG, "SOME TIME FETCHING ERROR $t")
                continuation.resumeWith(CoroutineResult.success(Result.retry()))
            }

            override fun onResponse(call: Call<ServerTime>, response: Response<ServerTime>) {
                val serverCal = Calendar.getInstance()
                serverCal.timeInMillis = response.body()!!.timeInMillis!!
                GlobalScope.launch {
                    wordOfTheDay = repo.getJustNonLive(
                        CalenderUtil.convertCalenderToString(
                            serverCal,
                            CalenderUtil.DATE_FORMAT
                        )
                    )
                    Log.d(TAG, Gson().toJson(wordOfTheDay) + "PRINT DATA BEFORE FETCHING")
                    if (wordOfTheDay == null) {
                        Log.d(TAG, "GETTING FROM SERVER")
                        apiService.getWordOfTheDay().enqueue(object :
                            Callback<ApiResponse<WordOfTheDay>> {

                            override fun onResponse(
                                call: Call<ApiResponse<WordOfTheDay>>,
                                response: Response<ApiResponse<WordOfTheDay>>
                            ) {
                                val apiResponse = response.body()
                                Log.d(TAG, Gson().toJson(apiResponse))
                                if (apiResponse != null && apiResponse.code == "200") {
                                    Log.d(TAG, "GOT FROMS SERVER, SHOW NOTIFICATION")
                                    GlobalScope.launch {
                                        val id = repo.addWord(apiResponse.data!!)
                                        Log.d(TAG, "Insert id: $id");
                                        //if word successfully inserted
                                        if (id > 0) {
                                            val notification =
                                                notificationHelper.createNotification(
                                                    title = "Here's your Today's Word",
                                                    body = apiResponse.data!!.word!!,
                                                    cancelable = true
                                                )
                                            notificationHelper.makeNotification(
                                                notification = notification
                                            )
/*
                                            //schedule word of the day reminder
                                            Log.d(TAG, "Schedule Word of the day reminder")
                                            repo.initRemindWordOfTheDay()
*/

                                            continuation.resumeWith(CoroutineResult.success(Result.success()))
                                        } else {
                                            Log.d(TAG, "Word already exits, retry");
                                            //since data is not inserted that mean word already exists
                                            //retry
                                            continuation.resumeWith(CoroutineResult.success(Result.retry()))
                                        }

                                    }

                                } else {
                                    continuation.resumeWith(CoroutineResult.success(Result.retry()))
                                }
                            }

                            override fun onFailure(
                                call: Call<ApiResponse<WordOfTheDay>>,
                                t: Throwable
                            ) {
                                Log.d(TAG, "onFailure: fetching word $t")
                                continuation.resumeWith(CoroutineResult.success(Result.retry()))
                            }
                        })
                    } else {
                        continuation.resumeWith(CoroutineResult.success(Result.success()))
                    }

                }
            }
        })
    }


}