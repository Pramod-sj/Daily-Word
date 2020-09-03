package com.pramod.dailyword.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.pramod.dailyword.Constants
import com.pramod.dailyword.db.repository.BookmarkRepo
import com.pramod.dailyword.helper.PronounceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class WordWidgetProvider : AppWidgetProvider() {
    private val TAG = WordWidgetProvider::class.simpleName

    companion object {
        const val ACTION_AUTO_UPDATE_WIDGET =
            "com.pramod.dailyword.ui.widget.WordWidgetProvider.ACTION_AUTO_UPDATE_WIDGET"
        const val ACTION_TRY_AGAIN_FROM_WIDGET =
            "com.pramod.dailyword.ui.widget.WordWidgetProvider.ACTION_TRY_AGAIN_FROM_WIDGET"
        const val ACTION_PLAY_AUDIO_FROM_WIDGET =
            "com.pramod.dailyword.ui.widget.WordWidgetProvider.ACTION_PLAY_AUDIO_FROM_WIDGET"
        const val ACTION_BOOKMARK_FROM_WIDGET =
            "com.pramod.dailyword.ui.widget.WordWidgetProvider.ACTION_BOOKMARK_FROM_WIDGET"

        const val EXTRA_AUDIO_URL = "audio_url"
        const val EXTRA_BOOKMARKED_WORD = "bookmarked_word"
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        Log.i(TAG, "onReceive: ${intent?.action}")
        intent?.let {
            when (it.action) {
                ACTION_PLAY_AUDIO_FROM_WIDGET -> {
                    Log.i(TAG, "onReceive: Playing")
                    it.getStringExtra(EXTRA_AUDIO_URL)?.let { audioUrl ->
                        PronounceHelper.playAudio(audioUrl)
                    }
                }
                ACTION_TRY_AGAIN_FROM_WIDGET -> {
                    context?.let { context ->
                        val jobInfo = JobInfo.Builder(
                            Constants.JOB_ID_FETCH_DATA_FOR_WIDGET,
                            ComponentName(context, WidgetDataLoadService::class.java)
                        ).build()

                        val jobScheduler: JobScheduler? =
                            context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

                        jobScheduler?.schedule(jobInfo)
                    }
                }
                ACTION_AUTO_UPDATE_WIDGET -> {
                    context?.let { context ->
                        val jobInfo = JobInfo.Builder(
                            Constants.JOB_ID_FETCH_DATA_FOR_WIDGET,
                            ComponentName(context, WidgetDataLoadService::class.java)
                        ).build()

                        val jobScheduler: JobScheduler? =
                            context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

                        jobScheduler?.schedule(jobInfo)
                    }
                }
                ACTION_BOOKMARK_FROM_WIDGET -> {
                    context?.let { context ->
                        GlobalScope.launch(Dispatchers.Main) {
                            val word = it.getStringExtra(EXTRA_BOOKMARKED_WORD)
                            word?.let { bookmarked_word ->
                                BookmarkRepo(context).bookmarkToggle(bookmarked_word)
                                //run data fetch job to get updated data
                                runDataFetchJob(context)
                            }
                        }
                    }
                }
                Intent.ACTION_TIME_CHANGED -> {
                    context?.let { context ->
                        //stopping currently running job and starting again
                        stopDataFetchJob(context)
                        runDataFetchJob(context)
                        //cancelling existing alarm and rescheduling
                        cancelRepeatingAlarm(context)
                        setRepeatingAlarm(context)
                    }
                }
                /*              Intent.ACTION_BOOT_COMPLETED -> {
                                  context?.let { context ->

                                      val appWidgetManager =
                                          context.getSystemService(Context.APPWIDGET_SERVICE) as AppWidgetManager
                                      Log.i(
                                          TAG,
                                          "onReceive: ${appWidgetManager.getAppWidgetIds(
                                              ComponentName(
                                                  context,
                                                  WordWidgetProvider::class.java
                                              )
                                          ).size}"
                                      )
              *//*
                    context?.let { context ->
                        runDataFetchJob(context)
                        setRepeatingAlarm(context)
                    }
*//*
                    }
                }*/
                else -> {
                }
            }
        }
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        Log.i(TAG, "onEnabled: ")
        context?.let {
            runDataFetchJob(it)
            setRepeatingAlarm(it)
        }
    }

    override fun onDisabled(context: Context?) {
        context?.let {
            stopDataFetchJob(it)
            cancelRepeatingAlarm(it)
        }
        super.onDisabled(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        val views =
            WidgetViewHelper.createWordOfTheDayWidget(
                context,
                null
            )
        appWidgetManager.updateAppWidget(widgetId, views)
    }

    private fun stopDataFetchJob(context: Context) {
        val jobScheduler: JobScheduler? =
            context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler?.cancel(Constants.JOB_ID_FETCH_DATA_FOR_WIDGET)
    }

    private fun cancelRepeatingAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WordWidgetProvider::class.java)
        intent.action =
            ACTION_AUTO_UPDATE_WIDGET
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            Constants.REQUEST_CODE_PENDING_INTENT_ALARM,
            intent,
            0
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun runDataFetchJob(context: Context) {
        val jobInfo = JobInfo.Builder(
            Constants.JOB_ID_FETCH_DATA_FOR_WIDGET,
            ComponentName(context, WidgetDataLoadService::class.java)
        ).build()

        val jobScheduler: JobScheduler? =
            context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        jobScheduler?.schedule(jobInfo)
    }

    private fun setRepeatingAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WordWidgetProvider::class.java)
        intent.action =
            ACTION_AUTO_UPDATE_WIDGET
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            Constants.REQUEST_CODE_PENDING_INTENT_ALARM,
            intent,
            0
        )

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 16)
        cal.set(Calendar.MINUTE, 15)

        if (cal.timeInMillis < Calendar.getInstance().timeInMillis) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC,
            cal.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

    }
}