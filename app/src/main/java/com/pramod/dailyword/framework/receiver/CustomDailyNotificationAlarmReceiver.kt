package com.pramod.dailyword.framework.receiver

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.ServiceCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.pramod.dailyword.business.data.network.Status
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.business.interactor.GetWordsInteractor
import com.pramod.dailyword.framework.firebase.FBMessageService
import com.pramod.dailyword.framework.firebase.FBMessageService.Companion.NOTIFICATION_NEW_WORD
import com.pramod.dailyword.framework.helper.NotificationHelper
import com.pramod.dailyword.framework.helper.safeImmutableFlag
import com.pramod.dailyword.framework.prefmanagers.NotificationPrefManager
import com.pramod.dailyword.framework.ui.common.exts.getLocalCalendar
import com.pramod.dailyword.framework.ui.settings.custom_time_notification.NotificationAlarmScheduler
import com.pramod.dailyword.framework.ui.splash_screen.SplashScreenActivity
import com.pramod.dailyword.framework.util.CalenderUtil
import dagger.assisted.Assisted
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class CustomDailyNotificationAlarmReceiver : BroadcastReceiver() {

    companion object {

        const val ACTION_CUSTOM_NOTIFICATION_TIME_ALARM =
            "com.pramod.dailyword.framework.receiver.ACTION_CUSTOM_NOTIFICATION_TIME_ALARM"

    }

    private val gson: Gson = Gson()


    @Inject
    lateinit var notificationAlarmScheduler: NotificationAlarmScheduler

    @Inject
    lateinit var notificationPrefManager: NotificationPrefManager

    @Inject
    lateinit var notificationHelper: NotificationHelper

    lateinit var getWordsInteractor: GetWordsInteractor

    override fun onReceive(context: Context?, intent: Intent?) {

        Timber.i("CustomNotificationAlarmReceiver: onReceive: ")

        if (intent?.action == ACTION_CUSTOM_NOTIFICATION_TIME_ALARM) {
            notificationPrefManager.getNotificationTriggerTimeNonLive()?.let { triggerTime ->
                val newTriggerTime = triggerTime.copy(
                    timeInMillis = getLocalCalendar().apply {
                        timeInMillis = triggerTime.timeInMillis
                        add(Calendar.DATE, 1)
                    }.timeInMillis
                )
                Timber.i(
                    "CustomNotificationAlarmReceiver: onReceive: scheduling alarm at " + CalenderUtil.convertCalenderToString(
                        newTriggerTime.timeInMillis, CalenderUtil.DATE_TIME_FORMAT_BEAUTIFY
                    )
                )
                notificationAlarmScheduler.scheduleAlarm(newTriggerTime)
                notificationPrefManager.setNotificationTriggerTime(newTriggerTime)
            }

            context?.let { _ ->
                notificationPrefManager.getNotificationMessagePayload()?.let { payload ->
                    //remove payload from preference for next notification
                    notificationPrefManager.setNotificationMessagePayload(null)
                    //show notification to the user
                    notificationHelper.showNotification(
                        createDailyWordNotification(
                            context,
                            payload
                        )
                    )
                }
            }
        }
    }

    private fun createDailyWordNotification(
        context: Context,
        payload: FBMessageService.MessagePayload
    ): Notification {

        val intentToActivity = Intent(context, SplashScreenActivity::class.java)
        intentToActivity.putExtra(FBMessageService.EXTRA_NOTIFICATION_PAYLOAD, gson.toJson(payload))
        intentToActivity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val pendingIntent = PendingIntent.getActivity(
            context,
            NotificationHelper.generateUniqueNotificationId(),
            intentToActivity,
            safeImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT)
        )

        //if word meaning show in notification is enable then return first
        //and if word meaning is null then return default body
        //or if word meaning show in notification is disable return default body
        val bodyText =
            if (notificationPrefManager.isShowingWordMeaningInNotification()) payload.wordMeaning
                ?: payload.body
            else payload.body

        return notificationHelper.createNotification(
            title = payload.title, body = bodyText, pendingIntent = pendingIntent
        )
    }

}