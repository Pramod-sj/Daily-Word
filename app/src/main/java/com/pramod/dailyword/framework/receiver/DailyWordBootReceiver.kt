package com.pramod.dailyword.framework.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pramod.dailyword.framework.helper.scheduleWeeklyAlarmAt12PM
import com.pramod.dailyword.framework.prefmanagers.NotificationPrefManager
import com.pramod.dailyword.framework.ui.common.exts.getLocalCalendar
import com.pramod.dailyword.framework.ui.settings.custom_time_notification.NotificationAlarmScheduler
import com.pramod.dailyword.framework.util.CalenderUtil
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject


@AndroidEntryPoint
class DailyWordBootReceiver : BroadcastReceiver() {


    companion object {
        val TAG = DailyWordBootReceiver::class.java.simpleName
    }

    @Inject
    lateinit var notificationAlarmScheduler: NotificationAlarmScheduler

    @Inject
    lateinit var notificationPrefManager: NotificationPrefManager

    override fun onReceive(context: Context?, intent: Intent?) {

        Timber.i("onReceive: ")

        if (intent?.action == Intent.ACTION_BOOT_COMPLETED
            || intent?.action == Intent.ACTION_LOCKED_BOOT_COMPLETED //
        ) {

            //scheduling weekly alarm after boot
            context?.scheduleWeeklyAlarmAt12PM()

            //schedule daily alarm based on notificationPrefManager.getNotificationTriggerTimeNonLive()
            rescheduleDailyNotificationAlarm()
        }
    }

    private fun rescheduleDailyNotificationAlarm() {
        notificationPrefManager.getNotificationTriggerTimeNonLive()
            ?.let { triggerTime ->
                val triggerTimeInMillis = triggerTime.timeInMillis
                if (triggerTimeInMillis > getLocalCalendar().timeInMillis) {
                    notificationAlarmScheduler.scheduleAlarm(triggerTime)
                    Timber.i(
                        "CustomNotificationAlarmReceiver: onReceive: rescheduling alarm at "
                                + CalenderUtil.convertCalenderToString(
                            triggerTime.timeInMillis,
                            CalenderUtil.DATE_TIME_FORMAT_BEAUTIFY
                        ) + " after boot"
                    )
                } else {
                    val newTriggerTime = triggerTime.copy(
                        timeInMillis = getLocalCalendar().apply {
                            timeInMillis = triggerTime.timeInMillis
                            add(Calendar.DATE, 1)
                        }.timeInMillis
                    )
                    notificationAlarmScheduler.scheduleAlarm(newTriggerTime)
                    notificationPrefManager.setNotificationTriggerTime(newTriggerTime)
                    Timber.i(
                        "CustomNotificationAlarmReceiver: onReceive: scheduling new alarm at " + CalenderUtil.convertCalenderToString(
                            newTriggerTime.timeInMillis,
                            CalenderUtil.DATE_TIME_FORMAT_BEAUTIFY
                        ) + " after boot"
                    )
                }
            }
    }
}