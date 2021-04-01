package com.pramod.dailyword.framework.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import com.google.gson.Gson
import com.pramod.dailyword.business.interactor.bookmark.ToggleBookmarkInteractor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
@AndroidEntryPoint
open class BaseWidgetProvider : AppWidgetProvider() {
    private val TAG = BaseWidgetProvider::class.simpleName

    @Inject
    lateinit var toggleBookmarkInteractor: ToggleBookmarkInteractor

    companion object {
        const val ACTION_AUTO_UPDATE_WIDGET =
            "com.pramod.dailyword.ui.widget.BaseWidgetProvider.ACTION_AUTO_UPDATE_WIDGET"
        const val ACTION_TRY_AGAIN_FROM_WIDGET =
            "com.pramod.dailyword.ui.widget.BaseWidgetProvider.ACTION_TRY_AGAIN_FROM_WIDGET"
        const val ACTION_BOOKMARK_FROM_WIDGET =
            "com.pramod.dailyword.ui.widget.BaseWidgetProvider.ACTION_BOOKMARK_FROM_WIDGET"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        intent?.let {
            Log.i(TAG, "onReceive: " + it.action)
            when (it.action) {
                Intent.ACTION_TIME_CHANGED -> {
                    context?.let { context ->
                        //stopping currently running job and starting again
                        stopTodayWordFetchJob(context)
                        runTodayWordFetchJob(context)
                        //cancelling existing alarm and rescheduling
                        cancelRepeatingAlarm(context)
                        setRepeatingDailyAlarmToFetch(context)
                    }
                }

                ACTION_TRY_AGAIN_FROM_WIDGET, ACTION_AUTO_UPDATE_WIDGET -> {
                    context?.let { context ->
                        runTodayWordFetchJob(context)
                    }
                }

                ACTION_BOOKMARK_FROM_WIDGET -> {
                    context?.let { context ->
                        GlobalScope.launch(Dispatchers.Main) {
                            val word = it.getStringExtra(WordWidgetProvider.EXTRA_BOOKMARKED_WORD)
                            word?.let { bookmarked_word ->
                                toggleBookmarkInteractor.toggle(bookmarked_word)
                                    .collectLatest {
                                        Log.i(TAG, "toggle: " + Gson().toJson(it))
                                    }
                                //run data fetch job to get updated data
                                runTodayWordFetchJob(context)
                            }
                        }
                    }
                }

                else -> {
                }
            }
        }
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

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        context?.let { context1 ->
            runTodayWordFetchJob(context1)
        }
        Log.i(TAG, "onUpdate: function executed")
    }

}