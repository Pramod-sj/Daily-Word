package com.pramod.dailyword.framework.firebase

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkedWordCacheDataSource
import com.pramod.dailyword.business.data.cache.abstraction.WordCacheDataSource
import com.pramod.dailyword.framework.prefmanagers.NotificationPrefManager
import com.pramod.dailyword.framework.ui.home.HomeActivity
import com.pramod.dailyword.framework.helper.NotificationHelper
import com.pramod.dailyword.framework.util.CalenderUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class FBMessageService : FirebaseMessagingService() {

    @Inject
    lateinit var wordCacheDataSource: WordCacheDataSource

    @Inject
    lateinit var bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource

    companion object {
        const val EXTRA_NOTIFICATION_PAYLOAD = "notification_payload"

        const val NOTIFICATION_NEW_WORD = "new_word"
        const val NOTIFICATION_REMINDER = "reminder"

        const val DEEP_LINK_TO_HOME_ACTIVITY = "/home"
        const val DEEP_LINK_TO_WORD_DETAILED = "/home/word_detail"
        const val DEEP_LINK_TO_WORD_LIST = "/home/word_list"
    }

    val TAG = FBMessageService::class.java.simpleName
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.i(TAG, "New Token: $p0")
    }

    @ExperimentalPagingApi
    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        val notificationPrefManager = NotificationPrefManager.newInstance(baseContext)
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

            GlobalScope.launch {
                var notification: Notification? = null
                if (payload.noitificationType == NOTIFICATION_REMINDER) {

                    Log.i(
                        TAG,
                        "onMessageReceived: isReminderNotificationEnabled: " + notificationPrefManager.isReminderNotificationEnabled()
                    )
                    if (!notificationPrefManager.isReminderNotificationEnabled()) {
                        return@launch
                    }


                    val word = bookmarkedWordCacheDataSource.getWordNonLive(
                        CalenderUtil.convertCalenderToString(
                            Calendar.getInstance(Locale.US),
                            CalenderUtil.DATE_FORMAT
                        )
                    )
                    //Log.i(TAG, Gson().toJson(wordOfTheDay))

                    //checking whether word seen or not
                    word?.let {
                        if (!it.isSeen) {
                            notification = notificationHelper
                                .createNotification(
                                    title = payload.title,
                                    body = payload.body,
                                    pendingIntent = pendingIntent
                                )
                        }
                    }


                } else if (payload.noitificationType == NOTIFICATION_NEW_WORD) {

                    if (!notificationPrefManager.isDailyWordNotificationEnabled()) {
                        return@launch
                    }

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
                    notificationHelper.showNotification(it)
                }

            }
        }
    }

    data class MessagePayload(
        var title: String = "Title",
        var body: String = "Body",
        var noitificationType: String = NOTIFICATION_NEW_WORD,
        var date: String = CalenderUtil.convertCalenderToString(Calendar.getInstance(Locale.US)),
        var deepLink: String = DEEP_LINK_TO_HOME_ACTIVITY
    )

}



