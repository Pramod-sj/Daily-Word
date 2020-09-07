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
        const val ACTION_PLAY_AUDIO_FROM_WIDGET =
            "com.pramod.dailyword.ui.widget.WordWidgetProvider.ACTION_PLAY_AUDIO_FROM_WIDGET"

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
                else -> {
                }
            }
        }
    }

}