package com.pramod.dailyword.framework.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.*
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import com.google.gson.Gson
import com.library.audioplayer.AudioPlayer
import com.pramod.dailyword.business.data.network.Status
import com.pramod.dailyword.business.interactor.bookmark.ToggleBookmarkInteractor
import com.pramod.dailyword.framework.ui.common.exts.safeLet
import com.pramod.dailyword.framework.widget.pref.WidgetSizePref
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
open class DailyWordWidgetProvider : AppWidgetProvider() {
    private val TAG = DailyWordWidgetProvider::class.simpleName

    @Inject
    lateinit var audioPlayer: AudioPlayer

    @Inject
    lateinit var toggleBookmarkInteractor: ToggleBookmarkInteractor

    @Inject
    lateinit var updateWidgetViewHelper: UpdateWidgetViewHelper

    @Inject
    lateinit var widgetSizePref: WidgetSizePref

    @Inject
    lateinit var widgetDataFetchHelper: WidgetDataFetchHelper

    @Inject
    lateinit var widgetPeriodicAlarmScheduler: WidgetPeriodicAlarmScheduler

    companion object {

        const val EXTRA_INTENT_TO_HOME_WORD_DATE = "word_date"

        const val ACTION_AUTO_UPDATE_WIDGET =
            "com.pramod.dailyword.ui.widget.BaseWidgetProvider.ACTION_AUTO_UPDATE_WIDGET"
        const val ACTION_TRY_AGAIN_FROM_WIDGET =
            "com.pramod.dailyword.ui.widget.BaseWidgetProvider.ACTION_TRY_AGAIN_FROM_WIDGET"
        const val ACTION_BOOKMARK_FROM_WIDGET =
            "com.pramod.dailyword.ui.widget.BaseWidgetProvider.ACTION_BOOKMARK_FROM_WIDGET"
        const val ACTION_PLAY_AUDIO_FROM_WIDGET =
            "com.pramod.dailyword.ui.widget.WordWidgetProvider.ACTION_PLAY_AUDIO_FROM_WIDGET"


        /**
         * This action will only make a network call when no word present in local,
         * it is introduce to load word from cache into widget when user open [HomeActivity]
         */
        const val ACTION_SILENT_REFRESH_WIDGET =
            "com.pramod.dailyword.ui.widget.BaseWidgetProvider.ACTION_SILENT_REFRESH_WIDGET"

        const val EXTRA_AUDIO_URL = "audio_url"
        const val EXTRA_BOOKMARKED_WORD = "bookmarked_word"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        intent?.let {
            context!!
            Timber.i("onReceive: " + it.action)
            Timber.i("onReceive: " + getWidthAndHeight(context, it.extras).toString())
            //Toast.makeText(context, it.action, Toast.LENGTH_SHORT).show()
            when (it.action) {
                Intent.ACTION_TIME_CHANGED -> {
                    //stopping currently running job and starting again
                    widgetDataFetchHelper.stopTodayWordFetchJob()
                    widgetDataFetchHelper.runTodayWordFetchJob()
                    //cancelling existing alarm and rescheduling
                    widgetPeriodicAlarmScheduler.cancelRepeatingAlarm()
                    widgetPeriodicAlarmScheduler.setRepeatingDailyAlarmToFetch()
                }

                ACTION_TRY_AGAIN_FROM_WIDGET, ACTION_AUTO_UPDATE_WIDGET -> {
                    widgetDataFetchHelper.runTodayWordFetchJob()
                }

                ACTION_BOOKMARK_FROM_WIDGET -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        val word = it.getStringExtra(EXTRA_BOOKMARKED_WORD)
                        word?.let { bookmarked_word ->
                            toggleBookmarkInteractor.toggle(bookmarked_word).collectLatest {
                                Timber.i("toggle: " + Gson().toJson(it))
                                if (it.status != Status.LOADING)
                                    updateWidgetViewHelper.updateWidgetUi(false)
                            }
                        }
                    }
                }
                ACTION_PLAY_AUDIO_FROM_WIDGET -> {
                    Timber.i("onReceive: Playing")
                    it.getStringExtra(EXTRA_AUDIO_URL)?.let { audioUrl ->
                        audioPlayer.play(audioUrl)
                    }
                }

                ACTION_SILENT_REFRESH_WIDGET -> {
                    widgetDataFetchHelper.runTodayWordFetchJob(false)
                }
                else -> {
                }
            }
        }
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        Timber.i("onEnabled: ")
        widgetPeriodicAlarmScheduler.setRepeatingDailyAlarmToFetch()
    }

    override fun onDisabled(context: Context?) {
        Timber.i("onDisabled: ")
        widgetDataFetchHelper.stopTodayWordFetchJob()
        widgetPeriodicAlarmScheduler.cancelRepeatingAlarm()
        super.onDisabled(context)
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        safeLet(appWidgetIds?.firstOrNull(), context) { appWidgetId, notNullContext ->
            widgetSizePref.setWidgetSize(
                getWidthAndHeight(
                    notNullContext,
                    appWidgetManager?.getAppWidgetOptions(appWidgetId)
                )
            )

            widgetDataFetchHelper.runTodayWordFetchJob()
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
        val widgetSize = getWidthAndHeight(context!!, newOptions)
        widgetSizePref.setWidgetSize(widgetSize) //storing current widget size in shared pref
        CoroutineScope(Dispatchers.IO).launch {
            updateWidgetViewHelper.updateWidgetUi(false)
        }
    }

    private fun getWidthAndHeight(
        context: Context,
        newOptions: Bundle?
    ): WidgetSizePref.WidgetSize {
        val width: Int
        val height: Int
        if (context.resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            || context.resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        ) {
            width = newOptions?.getInt(OPTION_APPWIDGET_MIN_WIDTH) ?: 0
            height = newOptions?.getInt(OPTION_APPWIDGET_MAX_HEIGHT) ?: 0
        } else {
            width = newOptions?.getInt(OPTION_APPWIDGET_MAX_WIDTH) ?: 0
            height = newOptions?.getInt(OPTION_APPWIDGET_MIN_HEIGHT) ?: 0
        }
        return WidgetSizePref.WidgetSize(width, height)
    }

}