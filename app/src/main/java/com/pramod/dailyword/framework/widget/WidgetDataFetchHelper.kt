package com.pramod.dailyword.framework.widget

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import com.pramod.dailyword.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetDataFetchHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val jobScheduler: JobScheduler
) {

    fun runTodayWordFetchJob() {
        if (isTodayWordFetchJobSchedule()) {
            Timber.i("Job already schedule or running")
            return
        }

        val jobInfo = JobInfo.Builder(
            Constants.JOB_ID_FETCH_DATA_FOR_WIDGET,
            ComponentName(context, WidgetDataLoadService::class.java)
        ).setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(false)
            .setOverrideDeadline(0)
            .build()

        jobScheduler.schedule(jobInfo)

        Timber.i("Job schedule successfully")
    }

    fun isTodayWordFetchJobSchedule(): Boolean {
        return jobScheduler.allPendingJobs.any { it.id == Constants.JOB_ID_FETCH_DATA_FOR_WIDGET }
    }

    fun stopTodayWordFetchJob() {
        jobScheduler.cancel(Constants.JOB_ID_FETCH_DATA_FOR_WIDGET)
    }

}