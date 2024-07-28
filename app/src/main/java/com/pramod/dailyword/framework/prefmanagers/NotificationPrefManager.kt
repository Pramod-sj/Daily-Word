package com.pramod.dailyword.framework.prefmanagers

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.google.gson.Gson
import com.pramod.dailyword.framework.firebase.FBMessageService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationPrefManager @Inject constructor(@ApplicationContext context: Context) :
    BasePreferenceManager(PREFERENCES_NAME, context) {


    companion object {
        private const val PREFERENCES_NAME = "notification_preferences"
        private const val KEY_DAILY_WORD_NOTIFICATION_ENABLED = "daily_word_notification_enabled"
        private const val KEY_REMINDER_NOTIFICATION_ENABLED = "reminder_notification_enabled"
        private const val KEY_SHOW_MEANING_IN_NOTIFICATION = "show_meaning_in_notification"
        private const val KEY_NOTIFICATION_TRIGGER_TIME = "notification_trigger_time"
        private const val KEY_NOTIFICATION_PAYLOAD = "notification_payload"


        private const val DEFAULT_VALUE_SHOW_MEANING_IN_NOTIFICATION = false

        fun newInstance(context: Context) = NotificationPrefManager(context)
    }

    private val gson = Gson()


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


    fun toggleShowWordMeaningInNotification() {
        editor.putBoolean(
            KEY_SHOW_MEANING_IN_NOTIFICATION,
            !isShowingWordMeaningInNotification()
        ).apply()
    }

    fun isShowingWordMeaningInNotification() = sPrefManager.getBoolean(
        KEY_SHOW_MEANING_IN_NOTIFICATION,
        DEFAULT_VALUE_SHOW_MEANING_IN_NOTIFICATION
    )

    fun getShowWordMeaningInNotificationLiveData(): SPrefBooleanLiveData {
        return SPrefBooleanLiveData(
            sPrefManager,
            KEY_SHOW_MEANING_IN_NOTIFICATION,
            DEFAULT_VALUE_SHOW_MEANING_IN_NOTIFICATION
        )
    }

    data class NotificationTriggerTime(
        val timeInMillis: Long,
        val isNextDay: Boolean,
    ) : Serializable

    fun setNotificationTriggerTime(time: NotificationTriggerTime?) {
        if (time == null) editor.remove(KEY_NOTIFICATION_TRIGGER_TIME).commit()
        else editor.putString(KEY_NOTIFICATION_TRIGGER_TIME, gson.toJson(time)).commit()
    }

    fun getNotificationTriggerTime(): LiveData<NotificationTriggerTime?> {
        return SPrefStringLiveData(
            sPrefManager, KEY_NOTIFICATION_TRIGGER_TIME, null
        ).map { gson.fromJson(it, NotificationTriggerTime::class.java) }
    }

    fun getNotificationTriggerTimeNonLive(): NotificationTriggerTime? {
        return sPrefManager.getString(KEY_NOTIFICATION_TRIGGER_TIME, null)?.let {
            gson.fromJson(it, NotificationTriggerTime::class.java)
        }
    }


    fun setNotificationMessagePayload(payload: FBMessageService.MessagePayload?) {
        if (payload == null) editor.remove(KEY_NOTIFICATION_PAYLOAD).commit()
        else editor.putString(KEY_NOTIFICATION_PAYLOAD, gson.toJson(payload)).commit()
    }

    fun getNotificationMessagePayload(): FBMessageService.MessagePayload? {
        return sPrefManager.getString(KEY_NOTIFICATION_PAYLOAD, null)?.let {
            gson.fromJson(it, FBMessageService.MessagePayload::class.java)
        }
    }

}