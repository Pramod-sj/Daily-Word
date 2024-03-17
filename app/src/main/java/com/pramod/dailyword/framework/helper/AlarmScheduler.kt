package com.pramod.dailyword.framework.helper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.core.app.AlarmManagerCompat
import com.pramod.dailyword.framework.receiver.ACTION_WEEKLY_12_PM_RECAP_WORDS_REMINDER
import com.pramod.dailyword.framework.receiver.AlarmReceiver
import com.pramod.dailyword.framework.ui.common.exts.isSunday
import com.pramod.dailyword.framework.ui.common.exts.make12AMInstance
import com.pramod.dailyword.framework.util.CalenderUtil
import timber.log.Timber
import java.util.*

fun safeImmutableFlag(flag: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE or flag
    } else flag
}


/**
 * Showing weekly notification fo revising words on every sunday
 */
fun Context.scheduleWeeklyAlarmAt12PM() {
    val comingSunday12PMCal = Calendar.getInstance(Locale.getDefault()).apply {
        if (isSunday()) {
            //if 12 PM for sunday passed then calculate
            // next upcoming sunday by adding 7 days
            val cal12PM = Calendar.getInstance().apply {
                make12AMInstance()
            }
            if (timeInMillis > cal12PM.timeInMillis) {
                val offset = 7
                add(Calendar.DATE, offset)
            }
        } else {
            val offset = 7 - (get(Calendar.DAY_OF_WEEK) - 1)
            add(Calendar.DATE, offset)
            Timber.i("scheduleWeeklyAlarmAt12PM: $offset")
        }
        make12AMInstance()
    }

    Timber.i(

        "scheduleWeeklyAlarmAt12PM: next alarm to be scheduled: ${
            CalenderUtil.convertCalenderToString(
                comingSunday12PMCal,
                CalenderUtil.DATE_TIME_FORMAT
            )
        }: id:${comingSunday12PMCal.timeInMillis.hashCode()}"
    )

    if (!isAlarmSchedule(comingSunday12PMCal.timeInMillis.hashCode())) {
        Timber.i("scheduleWeeklyAlarmAt12PM: scheduling 12 PM alarm")
        scheduleAlarm(
            timeInFuture = comingSunday12PMCal.timeInMillis,
            intent = Intent(
                this, AlarmReceiver::class.java
            ).setAction(ACTION_WEEKLY_12_PM_RECAP_WORDS_REMINDER)
        )
    } else {
        Timber.i("scheduleWeeklyAlarmAt12PM: not scheduling")
    }
}


fun Context.scheduleAlarm(
    timeInFuture: Long,
    alarmId: Int = timeInFuture.hashCode(),
    intent: Intent
) {

    val pendingIntent = PendingIntent.getBroadcast(
        this,
        alarmId,
        intent,
        safeImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT)
    )

    setCompactExactAndAllowWhileIdle(
        type = AlarmManager.RTC_WAKEUP,
        triggerAtMillis = timeInFuture,
        operation = pendingIntent
    )

}


fun Context.isAlarmSchedule(alarmId: Int): Boolean {
    val intent = Intent(this, AlarmReceiver::class.java)
    return PendingIntent.getBroadcast(
        this,
        alarmId,
        intent,
        safeImmutableFlag(PendingIntent.FLAG_NO_CREATE)
    ) != null
}


fun Context.setCompactExactAndAllowWhileIdle(
    type: Int,
    triggerAtMillis: Long,
    operation: PendingIntent
) {
    val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        when {
            // If permission is granted, proceed with scheduling exact alarms.
            alarmManager.canScheduleExactAlarms() -> {
                AlarmManagerCompat.setExactAndAllowWhileIdle(
                    alarmManager,
                    type,
                    triggerAtMillis,
                    operation
                )
            }

            else -> {
                // Ask users to go to exact alarm page in system settings. c
                // startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        }
    } else {
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            type, triggerAtMillis, operation
        )
    }
}