package com.pramod.dailyword.framework.widget

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Build
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class WidgetDataLoadService : JobService() {
    private val TAG = WidgetDataLoadService::class.simpleName

    companion object {
        const val EXTRA_SHOULD_CALL_API = "should_call_api"
    }

    @Inject
    lateinit var updateWidgetViewHelper: UpdateWidgetViewHelper

    private var updateUiJob: Job? = null

    override fun onStartJob(params: JobParameters?): Boolean {
        Timber.i("onStartJob: ")
        updateUiJob = CoroutineScope(Dispatchers.Main).launch {
            val shouldCallApi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                params?.extras?.getBoolean(EXTRA_SHOULD_CALL_API, true) ?: true
            } else true
            Timber.i("onStartJob: shouldCallApi: $shouldCallApi")
            updateWidgetViewHelper.fetchTodayWordAndUpdateWidgetUi(shouldCallApi)
            jobFinished(params, false)
        }
        return true //returning true so the work is handle in by the coroutine job (i.e. updateUiJob)
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Timber.i("onStopJob: ")
        return false // returning false as we don't want to reschedule this job
    }

    override fun onDestroy() {
        Timber.i("onDestroy: Widget Data Load Service Destroyed!")
        super.onDestroy()
        updateUiJob?.cancel()
        updateUiJob = null
    }
}