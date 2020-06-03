package com.pramod.todaysword.helper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.pramod.todaysword.R
import java.util.concurrent.atomic.AtomicInteger

class NotificationHelper(val context: Context) : ContextWrapper(context) {
    private val notificationManager: NotificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    companion object {
        private const val DEFAULT_NOTIFICATION_TITLE = "Today's Word Notification"
        private const val NOTIFICATION_CHANNEL = "com.pramod.todaysword.notification_channel"

        private val atomicInteger: AtomicInteger = AtomicInteger(1)

        fun generateUniqueNotificationId(): Int {
            return atomicInteger.incrementAndGet()
        }
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val notificationChannel: NotificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL,
            DEFAULT_NOTIFICATION_TITLE, NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.BLUE
        notificationChannel.setShowBadge(true)
        notificationChannel.setSound(uri, null)
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(notificationChannel)
    }


    fun createNotification(
        title: String = DEFAULT_NOTIFICATION_TITLE,
        body: String,
        cancelable: Boolean = true,
        pendingIntent: PendingIntent? = null
    ): Notification {
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(
            applicationContext,
            NOTIFICATION_CHANNEL
        )
        builder.setSmallIcon(R.mipmap.ic_launcher_round)
        builder.setContentTitle(title)
        builder.setContentText(body)
        builder.setAutoCancel(cancelable)
        builder.setSound(uri)
        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent)
        }
        return builder.build()
    }

    fun makeNotification(
        notification: Notification,
        notificationId: Int = generateUniqueNotificationId()
    ) {
        notificationManager.notify(notificationId, notification)
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

}