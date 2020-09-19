package com.pramod.dailyword.firebase

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.os.bundleOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.pramod.dailyword.db.repository.WOTDRepository
import com.pramod.dailyword.helper.NotificationHelper
import com.pramod.dailyword.ui.home.HomeActivity
import com.pramod.dailyword.ui.word_details.WordDetailedActivity
import com.pramod.dailyword.util.CalenderUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class FBMessageService : FirebaseMessagingService() {

    companion object {
        const val EXTRA_NOTIFICATION_PAYLOAD = "notification_payload"

        const val NOTIFICATION_NEW_WORD = "new_word"
        const val NOTIFICATION_REMINDER = "reminder"

        const val DEEP_LINK_TO_HOME_ACTIVITY = "/home"
        const val DEEP_LINK_TO_WORD_DETAILED = "/home/word_detail"
        const val DEEP_LINK_TO_WORD_LIST = "/home/word_list"
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
            Log.i(TAG, Gson().toJson(payload))
            val notificationHelper = NotificationHelper(applicationContext)

            val intentToActivity = Intent(applicationContext, HomeActivity::class.java)
            intentToActivity.putExtra(EXTRA_NOTIFICATION_PAYLOAD, Gson().toJson(p0.data))
            intentToActivity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                NotificationHelper.generateUniqueNotificationId(),
                intentToActivity,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val repo = WOTDRepository(applicationContext)
            GlobalScope.launch {
                var notification: Notification? = null
                if (payload.noitificationType == NOTIFICATION_REMINDER) {

                    val wordOfTheDay = repo.getJustNonLive(
                        CalenderUtil.convertCalenderToString(
                            Calendar.getInstance(),
                            CalenderUtil.DATE_FORMAT
                        )
                    )
                    //Log.i(TAG, Gson().toJson(wordOfTheDay))

                    //checking whether word seen or not
                    if (wordOfTheDay == null || !wordOfTheDay.isSeen) {
                        notification = notificationHelper
                            .createNotification(
                                title = payload.title,
                                body = payload.body,
                                pendingIntent = pendingIntent
                            )
                    }

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

    data class MessagePayload(
        var title: String = "Title",
        var body: String = "Body",
        var noitificationType: String = NOTIFICATION_NEW_WORD,
        var date: String = CalenderUtil.convertCalenderToString(Calendar.getInstance()),
        var deepLink: String = DEEP_LINK_TO_HOME_ACTIVITY
    )

}



