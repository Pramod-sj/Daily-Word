package com.pramod.dailyword.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.pramod.dailyword.db.repository.WOTDRepository
import com.pramod.dailyword.helper.NotificationHelper
import com.pramod.dailyword.ui.home.HomeActivity
import com.pramod.dailyword.util.CalenderUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class TodaysWordFirebaseMessageService : FirebaseMessagingService() {

    companion object {
        const val NOTIFICATION_NEW_WORD = "new_word"
        const val NOTIFICATION_REMINDER = "reminder"
    }

    val TAG = "FirebaseMessageService"
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.i(TAG, "New Token: $p0")
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        p0.let {
            val payload: MessagePayload =
                Gson().fromJson(Gson().toJson(p0.data), MessagePayload::class.java)
            val notificationHelper = NotificationHelper(applicationContext)
            val intentToMainActivity = Intent(applicationContext, HomeActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                999,
                intentToMainActivity,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val repo = WOTDRepository(applicationContext)
            GlobalScope.launch {
                val wordOfTheDay = repo.getJustNonLive(
                    CalenderUtil.convertCalenderToString(
                        Calendar.getInstance(),
                        CalenderUtil.DATE_FORMAT
                    )
                )
                var notification: Notification? = null
                if (wordOfTheDay != null && payload.noitificationType == NOTIFICATION_REMINDER) {
                    //checking whether word seen or not
                    if (!wordOfTheDay.isSeen) {
                        notification = notificationHelper
                            .createNotification(
                                title = payload.title,
                                body = payload.body,
                                pendingIntent = pendingIntent
                            )
                    }

                } else if (wordOfTheDay != null && payload.noitificationType == NOTIFICATION_NEW_WORD) {
                    notification = notificationHelper
                        .createNotification(
                            title = payload.title,
                            body = payload.body,
                            pendingIntent = pendingIntent
                        )
                } else {
                    //if no word in db or something diff notification type
                    notification = notificationHelper
                        .createNotification(
                            title = payload.title,
                            body = payload.body,
                            pendingIntent = pendingIntent
                        )
                }

                notification?.let {
                    notificationHelper.makeNotification(it)
                }

            }
        }
    }

    class MessagePayload {
        var title: String = "Title"
        var body: String = "Body"
        var noitificationType: String = NOTIFICATION_NEW_WORD
    }

}



