package com.pramod.dailyword.helper

import android.content.Context
import android.util.Log
import com.pramod.dailyword.firebase.FBTopicSubscriber

class NotificationPrefManager private constructor(private val context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()


    companion object {
        private const val PREFERENCES_NAME = "notification_preferences"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"

        fun newInstance(context: Context) = NotificationPrefManager(context)
    }


    fun toggleNotificationEnabled(listener: ((String, FBTopicSubscriber.OperationStatus) -> Unit)? = null) {
        Log.i("Notification TOGGLE", (!isNotificationEnabled()).toString())
        FBTopicSubscriber.toggleReceivingDailyWordNotification(this) { s: String, operationStatus: FBTopicSubscriber.OperationStatus ->
            if (operationStatus == FBTopicSubscriber.OperationStatus.SUCCESS) {
                editor.putBoolean(
                    KEY_NOTIFICATION_ENABLED,
                    !isNotificationEnabled()
                ).commit()
                listener?.invoke(s, FBTopicSubscriber.OperationStatus.SUCCESS)
            } else {
                listener?.invoke(s, FBTopicSubscriber.OperationStatus.FAILED)
            }
        }

    }


    fun isNotificationEnabled() = sharedPreferences.getBoolean(
        KEY_NOTIFICATION_ENABLED,
        true
    )


    fun getLiveData(): SPrefBooleanLiveData {
        return SPrefBooleanLiveData(
            sharedPreferences,
            KEY_NOTIFICATION_ENABLED, true
        )
    }

}