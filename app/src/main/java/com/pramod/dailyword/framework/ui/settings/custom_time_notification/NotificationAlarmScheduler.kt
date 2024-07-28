package com.pramod.dailyword.framework.ui.settings.custom_time_notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.pramod.dailyword.framework.helper.isAlarmSchedule
import com.pramod.dailyword.framework.helper.safeImmutableFlag
import com.pramod.dailyword.framework.helper.setCompactExactAndAllowWhileIdle
import com.pramod.dailyword.framework.prefmanagers.NotificationPrefManager
import com.pramod.dailyword.framework.receiver.CustomDailyNotificationAlarmReceiver
import com.pramod.dailyword.framework.receiver.CustomDailyNotificationAlarmReceiver.Companion.ACTION_CUSTOM_NOTIFICATION_TIME_ALARM
import com.pramod.dailyword.framework.ui.common.exts.getLocalCalendar
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject


class NotificationAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager
) {

    companion object {
        const val NOTIFICATION_ALARM_REQUEST_CODE =
            989898 //Static code as we will only have one alarm
    }

    fun scheduleAlarm(
        notificationTriggerTime: NotificationPrefManager.NotificationTriggerTime
    ) {

        if (context.isAlarmSchedule(NOTIFICATION_ALARM_REQUEST_CODE)) {
            Timber.i("Notification alarm is already set")
            return
        }

        if (notificationTriggerTime.timeInMillis < getLocalCalendar().timeInMillis) {
            Timber.i("Notification trigger time already passed!")
            return
        }

        val intent = Intent(context, CustomDailyNotificationAlarmReceiver::class.java)
            .setAction(ACTION_CUSTOM_NOTIFICATION_TIME_ALARM)

        val pendingIntent = PendingIntent.getBroadcast(
            /* context = */ context,
            /* requestCode = */ NOTIFICATION_ALARM_REQUEST_CODE,
            /* intent = */ intent,
            /* flags = */ safeImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT)
        )

        context.setCompactExactAndAllowWhileIdle(
            type = AlarmManager.RTC_WAKEUP,
            triggerAtMillis = notificationTriggerTime.timeInMillis,
            operation = pendingIntent
        )

        Timber.i("IS ALARM SET:" + context.isAlarmSchedule(NOTIFICATION_ALARM_REQUEST_CODE))
    }

    fun cancelAlarm() {

        val intent = Intent(context, CustomDailyNotificationAlarmReceiver::class.java)
            .setAction(ACTION_CUSTOM_NOTIFICATION_TIME_ALARM)

        val pendingIntent = PendingIntent.getBroadcast(
            /* context = */ context,
            /* requestCode = */ NOTIFICATION_ALARM_REQUEST_CODE,
            /* intent = */ intent,
            /* flags = */ safeImmutableFlag(0)
        )
        alarmManager.cancel(pendingIntent)
    }

}


