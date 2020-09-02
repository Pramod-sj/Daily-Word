package com.pramod.dailyword.firebase

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.pramod.dailyword.helper.NotificationPrefManager

class FBTopicSubscriber {
    enum class OperationStatus {
        SUCCESS,
        FAILED
    }

    companion object {
        private val TAG = FBTopicSubscriber::class.simpleName
        private val TOPIC_DAILY_WORD_NOTIFICATION = "daily_word_notification"

        fun toggleReceivingDailyWordNotification(
            notificationPrefManager: NotificationPrefManager,
            listener: ((String, FBTopicSubscriber.OperationStatus) -> Unit)? = null
        ) {
            if (notificationPrefManager.isNotificationEnabled()) {
                FBTopicSubscriber.stopReceivingDailyWordNotification { s, operationStatus ->
                    listener?.invoke(s, operationStatus)
                }
            } else {
                FBTopicSubscriber.startReceivingDailyWordNotification { s, operationStatus ->
                    listener?.invoke(s, operationStatus)
                }
            }
        }

        fun startReceivingDailyWordNotification(listener: ((String, OperationStatus) -> Unit)? = null) {
            FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_DAILY_WORD_NOTIFICATION)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i(TAG, "startReceivingDailyWordNotification: Success")
                        listener?.invoke(
                            "You'll now receive daily word notification every day",
                            OperationStatus.SUCCESS
                        )
                    } else {
                        Log.i(
                            TAG,
                            "startReceivingDailyWordNotification: Failed ${it.exception.toString()}"
                        )
                        listener?.invoke(
                            "There's some issue while registering you for notification service, Try again!",
                            OperationStatus.FAILED
                        )
                    }
                }
        }

        fun stopReceivingDailyWordNotification(listener: ((String, OperationStatus) -> Unit)? = null) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_DAILY_WORD_NOTIFICATION)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i(TAG, "stopReceivingDailyWordNotification: Success")
                        listener?.invoke(
                            "Your notification service is successfully disale",
                            OperationStatus.SUCCESS
                        )
                    } else {
                        Log.i(
                            TAG,
                            "stopReceivingDailyWordNotification: Failed ${it.exception.toString()}"
                        )
                        listener?.invoke(
                            "There's some issue while disaling notification service, Try again!",
                            OperationStatus.FAILED
                        )
                    }
                }
        }

        fun registerToCountryTopic() {

        }

        fun unregisterFromCountryTopic() {

        }
    }
}