package com.pramod.todaysword.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.pramod.todaysword.helper.NotificationHelper
import com.pramod.todaysword.ui.home.HomeActivity

class TodaysWordFirebaseMessageService : FirebaseMessagingService() {
    val TAG = "FirebaseMessageService"
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.i(TAG, "New Token: $p0")
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        p0.let{
            val payload : MessagePayload =
                Gson().fromJson(Gson().toJson(p0.data),MessagePayload::class.java)
            val notificationHelper = NotificationHelper(applicationContext)
            val intentToMainActivity = Intent(applicationContext,HomeActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                999,
                intentToMainActivity,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val notification = notificationHelper
                .createNotification(
                    title = payload.title,
                    body = payload.body,
                    pendingIntent = pendingIntent
                )
            notificationHelper.makeNotification(notification)
        }
    }

    class MessagePayload{
        var title :String = "Title"
        var body: String = "Body"
    }

}



