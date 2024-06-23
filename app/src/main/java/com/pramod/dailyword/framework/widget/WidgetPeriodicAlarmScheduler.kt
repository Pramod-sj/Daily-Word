package com.pramod.dailyword.framework.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.pramod.dailyword.Constants
import com.pramod.dailyword.framework.helper.safeImmutableFlag
import com.pramod.dailyword.framework.ui.common.exts.getLocalCalendar
import com.pramod.dailyword.framework.util.CalenderUtil
import com.pramod.dailyword.framework.util.CalenderUtil.Companion.DATE_TIME_FORMAT
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetPeriodicAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager
) {

    //An alarm is set at 5:00 PM for fetching new word
    private val alarmTimeHourOfDay = 17
    private val alarmTimeMinute = 0

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

        val cal = getLocalCalendar(alarmTimeHourOfDay, alarmTimeMinute)

        if (cal.timeInMillis < getLocalCalendar().timeInMillis) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        Timber.i("DAILYWORDDATE:"+ CalenderUtil.convertCalenderToString(cal, DATE_TIME_FORMAT))

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