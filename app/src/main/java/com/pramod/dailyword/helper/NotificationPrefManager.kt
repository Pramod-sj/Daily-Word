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
        private const val KEY_DAILY_WORD_NOTIFICATION_ENABLED = "daily_word_notification_enabled"
        private const val KEY_REMINDER_NOTIFICATION_ENABLED = "reminder_notification_enabled"

        fun newInstance(context: Context) = NotificationPrefManager(context)
    }


    fun toggleDailyWordNotification() {
        editor.putBoolean(
            KEY_DAILY_WORD_NOTIFICATION_ENABLED,
            !isDailyWordNotificationEnabled()
        ).commit()
    }


    fun isDailyWordNotificationEnabled() = sharedPreferences.getBoolean(
        KEY_DAILY_WORD_NOTIFICATION_ENABLED,
        true
    )


    fun getDailyWordNotificationEnabledLiveData(): SPrefBooleanLiveData {
        return SPrefBooleanLiveData(
            sharedPreferences,
            KEY_DAILY_WORD_NOTIFICATION_ENABLED, true
        )
    }


    fun toggleReminderNotification() {
        editor.putBoolean(
            KEY_REMINDER_NOTIFICATION_ENABLED,
            !isReminderNotificationEnabled()
        ).commit()
    }


    fun isReminderNotificationEnabled() = sharedPreferences.getBoolean(
        KEY_REMINDER_NOTIFICATION_ENABLED,
        true
    )


    fun getReminderNotificationEnabledLiveData(): SPrefBooleanLiveData {
        return SPrefBooleanLiveData(
            sharedPreferences,
            KEY_REMINDER_NOTIFICATION_ENABLED, true
        )
    }

}