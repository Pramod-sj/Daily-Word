package com.pramod.dailyword.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.pramod.dailyword.Constants
import com.pramod.dailyword.db.repository.BookmarkRepo
import com.pramod.dailyword.helper.PronounceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class WordWidgetProvider : BaseWidgetProvider() {
    private val TAG = WordWidgetProvider::class.simpleName

    companion object {
        const val ACTION_TRY_AGAIN_FROM_WIDGET =
            "com.pramod.dailyword.ui.widget.WordWidgetProvider.ACTION_TRY_AGAIN_FROM_WIDGET"
        const val ACTION_PLAY_AUDIO_FROM_WIDGET =
            "com.pramod.dailyword.ui.widget.WordWidgetProvider.ACTION_PLAY_AUDIO_FROM_WIDGET"
        const val ACTION_BOOKMARK_FROM_WIDGET =
            "com.pramod.dailyword.ui.widget.WordWidgetProvider.ACTION_BOOKMARK_FROM_WIDGET"

        const val EXTRA_AUDIO_URL = "audio_url"
        const val EXTRA_BOOKMARKED_WORD = "bookmarked_word"
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        Log.i(TAG, "onReceive: ${intent?.action}")
        intent?.let {
            when (it.action) {
                ACTION_PLAY_AUDIO_FROM_WIDGET -> {
                    Log.i(TAG, "onReceive: Playing")
                    it.getStringExtra(EXTRA_AUDIO_URL)?.let { audioUrl ->
                        PronounceHelper.playAudio(audioUrl)
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
                            val word = it.getStringExtra(EXTRA_BOOKMARKED_WORD)
                            word?.let { bookmarked_word ->
                                BookmarkRepo(context).bookmarkToggle(bookmarked_word)
                                //run data fetch job to get updated data
                                runTodayWordFetchJob(context)
                            }
                        }
                    }
                }
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
                else -> {
                }
            }
        }
    }


}