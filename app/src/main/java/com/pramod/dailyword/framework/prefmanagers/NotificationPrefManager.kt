package com.pramod.dailyword.framework.prefmanagers

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationPrefManager @Inject constructor(@ApplicationContext context: Context) :
    BasePreferenceManager(PREFERENCES_NAME, context) {


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


    fun isDailyWordNotificationEnabled() = sPrefManager.getBoolean(
        KEY_DAILY_WORD_NOTIFICATION_ENABLED,
        true
    )


    fun getDailyWordNotificationEnabledLiveData(): SPrefBooleanLiveData {
        return SPrefBooleanLiveData(
            sPrefManager,
            KEY_DAILY_WORD_NOTIFICATION_ENABLED, true
        )
    }


    fun toggleReminderNotification() {
        editor.putBoolean(
            KEY_REMINDER_NOTIFICATION_ENABLED,
            !isReminderNotificationEnabled()
        ).commit()
    }


    fun isReminderNotificationEnabled() = sPrefManager.getBoolean(
        KEY_REMINDER_NOTIFICATION_ENABLED,
        true
    )


    fun getReminderNotificationEnabledLiveData(): SPrefBooleanLiveData {
        return SPrefBooleanLiveData(
            sPrefManager,
            KEY_REMINDER_NOTIFICATION_ENABLED, true
        )
    }

}