package com.pramod.dailyword.framework.firebase

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import com.google.firebase.logger.Logger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.pramod.dailyword.business.data.cache.abstraction.BookmarkedWordCacheDataSource
import com.pramod.dailyword.business.data.cache.abstraction.WordCacheDataSource
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.framework.helper.NotificationHelper
import com.pramod.dailyword.framework.helper.safeImmutableFlag
import com.pramod.dailyword.framework.prefmanagers.NotificationPrefManager
import com.pramod.dailyword.framework.ui.common.exts.getLocalCalendar
import com.pramod.dailyword.framework.ui.splash_screen.SplashScreenActivity
import com.pramod.dailyword.framework.util.CalenderUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FBMessageService : FirebaseMessagingService() {

    @Inject
    lateinit var wordCacheDataSource: WordCacheDataSource

    @Inject
    lateinit var bookmarkedWordCacheDataSource: BookmarkedWordCacheDataSource

    @Inject
    lateinit var notificationPrefManager: NotificationPrefManager

    companion object {
        const val EXTRA_NOTIFICATION_PAYLOAD = "notification_payload"

        const val NOTIFICATION_NEW_WORD = "new_word"
        const val NOTIFICATION_REMINDER = "reminder"

        const val DEEP_LINK_TO_HOME_ACTIVITY = "/home"
        const val DEEP_LINK_TO_WORD_DETAILED = "/home/word_detail"
        const val DEEP_LINK_TO_WORD_LIST = "/home/word_list"
    }

    private val TAG = FBMessageService::class.java.simpleName

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Timber.i("New Token: $p0")
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        val notificationPrefManager = NotificationPrefManager.newInstance(baseContext)
        p0.let {
            val payload: MessagePayload =
                Gson().fromJson(Gson().toJson(p0.data), MessagePayload::class.java)
            Timber.i(Gson().toJson(payload))
            val notificationHelper = NotificationHelper(applicationContext)

            val intentToActivity = Intent(applicationContext, SplashScreenActivity::class.java)
            intentToActivity.putExtra(EXTRA_NOTIFICATION_PAYLOAD, Gson().toJson(p0.data))
            intentToActivity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                NotificationHelper.generateUniqueNotificationId(),
                intentToActivity,
                safeImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT)
            )

            CoroutineScope(Dispatchers.Main).launch {

                var notification: Notification? = null

                //getting first word meaning text
                val wordMeaning: String? = payload.wordMeaning?.split("||")?.firstOrNull()

                //Log.i( "onMessageReceived: " + Gson().toJson(wordMeaning))

                //if word meaning show in notification is enable then return first
                //and if word meaning is null then return default body
                //or if word meaning show in notification is disable return default body
                val bodyText =
                    if (notificationPrefManager.isShowingWordMeaningInNotification()) wordMeaning
                        ?: payload.body
                    else payload.body

                when (payload.noitificationType) {
                    NOTIFICATION_REMINDER -> {

                        if (!notificationPrefManager.isReminderNotificationEnabled()) {
                            return@launch
                        }

                        //Log.i( "onMessageReceived: reminder notification enabled")

                        val word: Word? = try {
                            bookmarkedWordCacheDataSource.getWordNonLive(payload.date)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }

                        Timber.i("onMessageReceived: ${word?.word} - ${word?.isSeen}")
                        //Log.i( Gson().toJson(wordOfTheDay))

                        //checking whether word seen or not
                        word?.let {
                            //Log.i( "onMessageReceived: inside let")
                            if (!it.isSeen) {
                                //Log.i( "onMessageReceived: not seen")
                                notification = notificationHelper.createNotification(
                                    title = payload.title,
                                    body = bodyText,
                                    pendingIntent = pendingIntent
                                )
                            }
                        }

                    }

                    NOTIFICATION_NEW_WORD -> {

                        if (notificationPrefManager.isDailyWordNotificationEnabled()) {

                            notificationPrefManager.setNotificationMessagePayload(payload)

                            notificationPrefManager.getNotificationTriggerTimeNonLive()
                                ?.let { notificationTriggerTime ->

                                    if (notificationTriggerTime.timeInMillis < System.currentTimeMillis()) {

                                        //if notification time is already elapsed then directly show the notification
                                        notification = notificationHelper.createNotification(
                                            title = payload.title,
                                            body = bodyText,
                                            pendingIntent = pendingIntent
                                        )

                                        //remove payload from preference for next notification
                                        notificationPrefManager.setNotificationMessagePayload(null)

                                        //create a notification object which needs to be shown
                                        notification = notificationHelper.createNotification(
                                            title = payload.title,
                                            body = bodyText,
                                            pendingIntent = pendingIntent
                                        )

                                    }
                                }

                        }

                    }

                    else -> {
                        //if no word in db or something diff notification type
                        notification = notificationHelper.createNotification(
                            title = payload.title,
                            body = bodyText,
                            pendingIntent = pendingIntent
                        )
                    }
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
        var date: String = CalenderUtil.convertCalenderToString(getLocalCalendar()),
        var deepLink: String = DEEP_LINK_TO_HOME_ACTIVITY,
        var wordMeaning: String? = null
    )

}



