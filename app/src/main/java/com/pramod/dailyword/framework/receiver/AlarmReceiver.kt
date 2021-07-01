package com.pramod.dailyword.framework.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pramod.dailyword.framework.helper.NotificationHelper
import com.pramod.dailyword.framework.helper.safeImmutableFlag
import com.pramod.dailyword.framework.ui.recap.RecapWordsActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val ACTION_WEEKLY_12_PM_RECAP_WORDS_REMINDER =
    "com.pramod.dailyword.framework.receiver.AlarmReceiver.ACTION_WEEKLY_12_PM_RECAP_WORDS_REMINDER"

const val REQUEST_CODE_WEEKLY_12_PM_RECAP_WORDS_REMINDER = 345

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ACTION_WEEKLY_12_PM_RECAP_WORDS_REMINDER -> {
                val notification = notificationHelper.createNotification(
                    title = "Good Afternoon folks!",
                    body = "Let's revise this week's words",
                    pendingIntent = PendingIntent.getActivity(
                        context,
                        REQUEST_CODE_WEEKLY_12_PM_RECAP_WORDS_REMINDER,
                        Intent(context, RecapWordsActivity::class.java),
                        safeImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT)
                    )
                )
                notificationHelper.showNotification(notification)
            }
        }
    }
}