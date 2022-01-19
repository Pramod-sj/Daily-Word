package com.pramod.dailyword.framework.widget

import android.app.job.JobParameters
import android.app.job.JobService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class WidgetRandomWordLoadService : JobService() {
    private val TAG = WidgetRandomWordLoadService::class.simpleName

    @Inject
    lateinit var updateWidgetViewHelper: UpdateWidgetViewHelper

    private var updateUiJob: Job? = null

    override fun onStartJob(params: JobParameters?): Boolean {
        Timber.i("onStartJob: ")
        updateUiJob = CoroutineScope(Dispatchers.Main).launch {
            updateWidgetViewHelper.fetchRandomWordAndUpdateWidgetUi()
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