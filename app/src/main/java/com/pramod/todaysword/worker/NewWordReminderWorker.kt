package com.pramod.todaysword.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import com.pramod.todaysword.ui.home.HomeActivity
import com.pramod.todaysword.util.CalenderUtil
import com.pramod.todaysword.util.NetworkUtils
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.coroutines.suspendCoroutine
import kotlin.Result as CoroutineResult

class NewWordReminderWorker(val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    private val localDb = AppDB.getInstance(context)
    private val notificationHelper: NotificationHelper = NotificationHelper(context)

    companion object {
        const val TAG = "NewWordReminderWorker"
    }

    override fun doWork(): Result {

        Handler(Looper.getMainLooper()).post {
            val workInfosLiveData =
                WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(TAG)
            val observer = object : Observer<List<WorkInfo>> {
                override fun onChanged(t: List<WorkInfo>) {
                    for (workInfo in t) {
                        Log.d(TAG, workInfo.state.toString());
                        if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                            //reschedule work when succeeded
                        }

                        if (workInfo.state == WorkInfo.State.CANCELLED
                            || workInfo.state == WorkInfo.State.FAILED
                            || workInfo.state == WorkInfo.State.SUCCEEDED
                        ) {
                            Log.d(TAG, "Removing livedata observer");
                            workInfosLiveData.removeObserver(this)
                        }
                    }
                }
            }
            workInfosLiveData.observeForever(observer)

        }

        if (runAttemptCount > 4) {
            return Result.failure();
        } else {
            val cal = Calendar.getInstance()
            val wordOfTheDay = localDb!!.getWordOfTheDayDao()
                .getJustNonLive(
                    CalenderUtil.convertCalenderToString(
                        cal,
                        CalenderUtil.DATE_FORMAT
                    )
                );
            if (wordOfTheDay == null) {
                Log.d(TAG, "GET WORD IF STILL NULL");
                //WOTDRepository(context).initGetWordOfTheDayWorker(true)
                return Result.retry();
            } else {
                if (!(wordOfTheDay.isSeen)) {
                    Log.d(TAG, "Show reminder notification")

                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        1,
                        Intent(context, HomeActivity::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )

                    val notification = notificationHelper.createNotification(
                        title = "You missed Today's Word",
                        body = wordOfTheDay!!.word!!, cancelable = true,
                        pendingIntent = pendingIntent
                    )
                    notificationHelper.makeNotification(
                        notification = notification
                    )
                }
                return Result.success();
            }
        }
    }


}