package com.pramod.dailyword.framework.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.*
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.paging.ExperimentalPagingApi
import com.google.gson.Gson
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkedWordCacheDataSource
import com.pramod.dailyword.business.interactor.bookmark.ToggleBookmarkInteractor
import com.pramod.dailyword.framework.util.CalenderUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
open class BaseWidgetProvider : AppWidgetProvider() {
    private val TAG = BaseWidgetProvider::class.simpleName

    @Inject
    lateinit var bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource

    @OptIn(ExperimentalPagingApi::class)
    @Inject
    lateinit var toggleBookmarkInteractor: ToggleBookmarkInteractor

    companion object {

        const val EXTRA_INTENT_TO_HOME_WORD_DATE = "word_date"

        const val ACTION_AUTO_UPDATE_WIDGET =
            "com.pramod.dailyword.ui.widget.BaseWidgetProvider.ACTION_AUTO_UPDATE_WIDGET"
        const val ACTION_TRY_AGAIN_FROM_WIDGET =
            "com.pramod.dailyword.ui.widget.BaseWidgetProvider.ACTION_TRY_AGAIN_FROM_WIDGET"
        const val ACTION_BOOKMARK_FROM_WIDGET =
            "com.pramod.dailyword.ui.widget.BaseWidgetProvider.ACTION_BOOKMARK_FROM_WIDGET"
    }

    @OptIn(ExperimentalPagingApi::class, DelicateCoroutinesApi::class)
    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        intent?.let {
            Timber.i("onReceive: " + it.action)
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
                                        Timber.i("toggle: " + Gson().toJson(it))
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
        Timber.i("onEnabled: ")
        context?.let {
            runTodayWordFetchJob(it)
            setRepeatingDailyAlarmToFetch(it)
        }
    }

    override fun onDisabled(context: Context?) {
        Timber.i("onDisabled: ")
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
        Timber.i("onUpdate: function executed")
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)

        val height: Int
        val width: Int
        if (context!!.resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            || context.resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        ) {
            width = newOptions?.getInt(OPTION_APPWIDGET_MIN_WIDTH) ?: 0
            height = newOptions?.getInt(OPTION_APPWIDGET_MAX_HEIGHT) ?: 0
        } else {
            width = newOptions?.getInt(OPTION_APPWIDGET_MAX_WIDTH) ?: 0
            height = newOptions?.getInt(OPTION_APPWIDGET_MIN_HEIGHT) ?: 0
        }

        Timber.i("Width:%s ; Height:%s", width, height)

        CoroutineScope(Dispatchers.IO).launch {
            val word = bookmarkedWordCacheDataSource.getWordNonLive(
                CalenderUtil.convertCalenderToString(Calendar.getInstance(Locale.US))
            )
            appWidgetManager?.updateAppWidget(
                appWidgetId,
                WidgetViewHelper.getRemoteViews(context, word, width, height)
            )
        }
    }
}