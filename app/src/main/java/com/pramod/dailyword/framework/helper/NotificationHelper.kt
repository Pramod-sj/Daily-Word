package com.pramod.dailyword.framework.helper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_SOUND
import androidx.core.app.NotificationCompat.DEFAULT_VIBRATE
import androidx.core.content.ContextCompat
import com.pramod.dailyword.R
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class NotificationHelper @Inject constructor(val context: Context) : ContextWrapper(context) {
    private val notificationManager: NotificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val defaultVibrationPattern = longArrayOf(0, 250, 250, 250)
    private val uri = Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(applicationContext.packageName)
        .path(R.raw.notification.toString())
        .build()
    /*val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)*/

    companion object {
        private const val TAG = "NotificationHelper"
        private const val DEFAULT_NOTIFICATION_TITLE = "Daily Word Notification"
        private const val NOTIFICATION_CHANNEL = "com.pramod.dailyword.notification_channel"

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
        notificationChannel.importance = NotificationManager.IMPORTANCE_HIGH
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.BLUE
        notificationChannel.setShowBadge(true)
        notificationChannel.vibrationPattern = defaultVibrationPattern
        val audioAttribute = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        notificationChannel.setSound(uri, audioAttribute)
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
        builder.setSmallIcon(R.drawable.ic_notification)
        builder.setContentTitle(title)
        builder.color = ContextCompat.getColor(applicationContext, R.color.colorPrimaryDesaturated)
        builder.setDefaults(DEFAULT_SOUND + DEFAULT_VIBRATE)
        builder.setContentText(body)
        builder.setAutoCancel(cancelable)
        builder.setSound(uri)
        builder.priority = NotificationCompat.PRIORITY_MAX
        builder.setVibrate(defaultVibrationPattern)
        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent)
        }
        return builder.build()
    }

    fun showNotification(
        notification: Notification,
        notificationId: Int = generateUniqueNotificationId()
    ) {
/*
        if (NotificationPrefManager.newInstance(context).isNotificationEnabled()) {
            notificationManager.notify(notificationId, notification)
        } else {
            Log.i(TAG, "Notification are disabled, enable it from settings")
        }
*/
        notificationManager.notify(notificationId, notification)
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

}