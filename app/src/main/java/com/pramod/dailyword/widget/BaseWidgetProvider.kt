package com.pramod.dailyword.widget

import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log

open class BaseWidgetProvider : AppWidgetProvider() {
    private val TAG = BaseWidgetProvider::class.simpleName

    companion object {
        const val ACTION_AUTO_UPDATE_WIDGET =
            "com.pramod.dailyword.ui.widget.BaseWidgetProvider.ACTION_AUTO_UPDATE_WIDGET"
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        Log.i(TAG, "onEnabled: ")
        context?.let {
            runTodayWordFetchJob(it)
            setRepeatingDailyAlarmToFetch(it)
        }
    }

    override fun onDisabled(context: Context?) {
        Log.i(TAG, "onDisabled: ")
        context?.let {
            stopTodayWordFetchJob(it)
            cancelRepeatingAlarm(it)
        }
        super.onDisabled(context)
    }

}