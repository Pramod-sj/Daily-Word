package com.pramod.dailyword.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.pramod.dailyword.R
import com.pramod.dailyword.ui.home.HomeActivity

class WidgetActivity : AppWidgetProvider() {


    private val TAG = WidgetActivity::class.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        Log.i(TAG, "onReceive: ")
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        Log.i(TAG, "onEnabled: ")
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }


    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        val views =
            RemoteViews(context.packageName, R.layout.widget_todays_word_layout)

        views.setTextViewText(R.id.widget_txtView_word_of_the_day, "Test")
        views.setTextViewText(R.id.widget_txtView_attribute, "Test Attribute")
        views.setTextViewText(R.id.widget_txtView_pronounce, "Test Pronounce")
        views.setTextViewText(R.id.widget_txtView_meanings, "Test Meanings")
        views.setTextViewText(R.id.widget_txtView_how_to_user_word, "How to use Test")
        views.setTextViewText(R.id.widget_txtView_how_to_user_word_desc, "Test word desc")


        val pendingIntentForWidgetClick = PendingIntent.getActivity(
            context,
            1,
            Intent(context, HomeActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.main_linearLayout_wotd, pendingIntentForWidgetClick)

        val playAudioIntent = Intent(context, OnPlayAudioClick::class.java)
        playAudioIntent.action = OnPlayAudioClick.ACTION_PLAY_AUDIO_FROM_WIDGET

        val pendingIntentPlayAudio = PendingIntent.getBroadcast(
            context,
            2,
            playAudioIntent,
            0
        )
        views.setOnClickPendingIntent(R.id.widget_img_pronounce, pendingIntentPlayAudio)

        appWidgetManager.updateAppWidget(widgetId, views)
    }

}

class OnPlayAudioClick : BroadcastReceiver() {
    private val TAG = OnPlayAudioClick::class.simpleName

    companion object {
        const val ACTION_PLAY_AUDIO_FROM_WIDGET = "play_audio_from_widget"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            Log.i(TAG, "onReceive: ${it.action}")
            if (it.action == ACTION_PLAY_AUDIO_FROM_WIDGET) {
                Log.i(TAG, "onReceive: Playing")
            }
        }

    }

}