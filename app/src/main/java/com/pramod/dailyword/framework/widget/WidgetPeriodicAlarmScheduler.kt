package com.pramod.dailyword.framework.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.pramod.dailyword.Constants
import com.pramod.dailyword.framework.helper.safeImmutableFlag
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetPeriodicAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager
) {

    //An alarm is set at 4:30 PM for fetching new word
    private val alarmTimeHourOfDay = 16
    private val alarmTimeMinute = 30

    fun setRepeatingDailyAlarmToFetch() {
        val intent = Intent(context, DailyWordWidgetProvider::class.java)
        intent.action =
            DailyWordWidgetProvider.ACTION_AUTO_UPDATE_WIDGET
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            Constants.REQUEST_CODE_PENDING_INTENT_ALARM,
            intent,
            safeImmutableFlag(0)
        )

        val cal = Calendar.getInstance(Locale.US)
        cal.set(Calendar.HOUR_OF_DAY, alarmTimeHourOfDay)
        cal.set(Calendar.MINUTE, alarmTimeMinute)

        if (cal.timeInMillis < Calendar.getInstance(Locale.US).timeInMillis) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC,
            cal.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

    }

    fun cancelRepeatingAlarm() {
        val intent = Intent(context, DailyWordWidgetProvider::class.java)
        intent.action =
            DailyWordWidgetProvider.ACTION_AUTO_UPDATE_WIDGET
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            Constants.REQUEST_CODE_PENDING_INTENT_ALARM,
            intent,
            safeImmutableFlag(0)
        )
        alarmManager.cancel(pendingIntent)
    }


}