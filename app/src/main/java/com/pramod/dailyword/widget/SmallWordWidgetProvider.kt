package com.pramod.dailyword.widget

import android.app.job.JobInfo
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log

class SmallWordWidgetProvider : BaseWidgetProvider() {

    val TAG = SmallWordWidgetProvider::class.simpleName

    companion object {
        const val ACTION_TRY_AGAIN_FROM_WIDGET =
            "com.pramod.dailyword.ui.widget.SmallWordWidgetProvider.ACTION_TRY_AGAIN_FROM_WIDGET"

    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        intent?.let {
            when (it.action) {
                ACTION_AUTO_UPDATE_WIDGET, ACTION_TRY_AGAIN_FROM_WIDGET -> {
                    context?.let { context ->
                        runTodayWordFetchJob(context)
                    }
                }
                else -> {
                }
            }
        }
    }

}