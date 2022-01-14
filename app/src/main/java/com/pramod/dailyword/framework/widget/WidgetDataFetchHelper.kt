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
            Timber.i("Job already schedule or running")
            return
        }

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
        }.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
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