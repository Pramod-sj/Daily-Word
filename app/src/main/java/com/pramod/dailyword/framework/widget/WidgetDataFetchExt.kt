package com.pramod.dailyword.framework.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.paging.ExperimentalPagingApi
import com.pramod.dailyword.Constants
import java.util.*


@OptIn(ExperimentalPagingApi::class)
fun BaseWidgetProvider.runTodayWordFetchJob(context: Context) {
    val jobInfo = JobInfo.Builder(
        Constants.JOB_ID_FETCH_DATA_FOR_WIDGET,
        ComponentName(context, WidgetDataLoadService::class.java)
    ).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        .setRequiresCharging(false).build()

    val jobScheduler: JobScheduler? =
        context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

    jobScheduler?.schedule(jobInfo)
}

fun BaseWidgetProvider.stopTodayWordFetchJob(context: Context) {
    val jobScheduler: JobScheduler? =
        context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    jobScheduler?.cancel(Constants.JOB_ID_FETCH_DATA_FOR_WIDGET)
}

fun BaseWidgetProvider.setRepeatingDailyAlarmToFetch(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, WordWidgetProvider::class.java)
    intent.action =
        BaseWidgetProvider.ACTION_AUTO_UPDATE_WIDGET
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        Constants.REQUEST_CODE_PENDING_INTENT_ALARM,
        intent,
        0
    )

    val cal = Calendar.getInstance(Locale.US)
    cal.set(Calendar.HOUR_OF_DAY, 16)
    cal.set(Calendar.MINUTE, 15)

    if (cal.timeInMillis < Calendar.getInstance(Locale.US).timeInMillis) {
        cal.add(Calendar.DAY_OF_YEAR, 1)
    }

    alarmManager.setInexactRepeating(
        AlarmManager.RTC,
        cal.timeInMillis,
        AlarmManager.INTERVAL_DAY,
        pendingIntent
    )

}

fun BaseWidgetProvider.cancelRepeatingAlarm(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, WordWidgetProvider::class.java)
    intent.action =
        BaseWidgetProvider.ACTION_AUTO_UPDATE_WIDGET
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        Constants.REQUEST_CODE_PENDING_INTENT_ALARM,
        intent,
        0
    )
    alarmManager.cancel(pendingIntent)
}


