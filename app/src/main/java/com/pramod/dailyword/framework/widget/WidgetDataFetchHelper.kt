package com.pramod.dailyword.framework.widget

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import androidx.core.os.persistableBundleOf
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

    fun runTodayWordFetchJob(shouldCallApi: Boolean = true) {
        if (isTodayWordFetchJobSchedule()) {
            Timber.i("Todays word Job already schedule or running")
            return
        }

        if (isRandomWordJobSchedule()) stopRandomWordJob() //cancelling random word job

        val jobInfo = JobInfo.Builder(
            Constants.JOB_ID_FETCH_DATA_FOR_WIDGET,
            ComponentName(context, WidgetDataLoadService::class.java)
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
                setExtras(
                    persistableBundleOf(
                        WidgetDataLoadService.EXTRA_SHOULD_CALL_API to shouldCallApi
                    )
                )
        }.setRequiredNetworkType(
            if (shouldCallApi) JobInfo.NETWORK_TYPE_ANY
            else JobInfo.NETWORK_TYPE_NONE
        ).setRequiresCharging(false)
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
        Timber.i("Cancel TodayWordFetchJob")
        jobScheduler.cancel(Constants.JOB_ID_FETCH_DATA_FOR_WIDGET)
    }

    fun runRandomWordJob() {
        if (isRandomWordJobSchedule()) {
            Timber.i("Random word Job already schedule or running")
            return
        }

        if (isTodayWordFetchJobSchedule()) stopTodayWordFetchJob() //cancelling random word job

        val jobInfo = JobInfo.Builder(
            Constants.JOB_ID_FETCH_RANDOM_WORD_FOR_WIDGET,
            ComponentName(context, WidgetRandomWordLoadService::class.java)
        ).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(false)
            .setOverrideDeadline(0)
            .build()

        jobScheduler.schedule(jobInfo)

    }

    fun isRandomWordJobSchedule(): Boolean {
        return jobScheduler.allPendingJobs.any { it.id == Constants.JOB_ID_FETCH_RANDOM_WORD_FOR_WIDGET }
    }

    fun stopRandomWordJob() {
        Timber.i("Cancel RandomWordJob")
        jobScheduler.cancel(Constants.JOB_ID_FETCH_RANDOM_WORD_FOR_WIDGET)
    }
}