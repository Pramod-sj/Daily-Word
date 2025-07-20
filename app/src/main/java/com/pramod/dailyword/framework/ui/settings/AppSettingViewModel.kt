package com.pramod.dailyword.framework.ui.settings

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.pramod.dailyword.R
import com.pramod.dailyword.business.data.network.Status
import com.pramod.dailyword.business.interactor.GetWordsInteractor
import com.pramod.dailyword.framework.firebase.FBMessageService
import com.pramod.dailyword.framework.firebase.FBMessageService.Companion.DEEP_LINK_TO_WORD_DETAILED
import com.pramod.dailyword.framework.firebase.FBMessageService.Companion.NOTIFICATION_NEW_WORD
import com.pramod.dailyword.framework.prefmanagers.EdgeToEdgeEnabler
import com.pramod.dailyword.framework.prefmanagers.NotificationPrefManager
import com.pramod.dailyword.framework.ui.common.BaseViewModel
import com.pramod.dailyword.framework.ui.common.exts.getLocalCalendar
import com.pramod.dailyword.framework.ui.settings.custom_time_notification.NotificationAlarmScheduler
import com.pramod.dailyword.framework.util.CalenderUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppSettingViewModel @Inject constructor(
    val notificationPrefManager: NotificationPrefManager,
    @ApplicationContext private val context: Context,
    private val notificationAlarmScheduler: NotificationAlarmScheduler,
    private val getWordsInteractor: GetWordsInteractor,
    private val edgeToEdgeEnabler: EdgeToEdgeEnabler
) : BaseViewModel() {

    var settingUseCase: SettingUseCase? = null

    val themeValue = MutableLiveData<String>()

    val windowAnimValue = MutableLiveData<Boolean>()

    val edgeToEdgeValue = MutableLiveData<Boolean>()

    val hideBadgesValue = MutableLiveData<Boolean>()

    val subTitleCheckForUpdate = MutableLiveData<String>()

    val changeNotificationSubtitle = MutableStateFlow("")

    val notificationTriggerTime =
        MutableStateFlow<NotificationPrefManager.NotificationTriggerTime?>(null)

    private val _notificationTriggerTimeChangeMessage = Channel<String>()
    val notificationTriggerTimeChangeMessage: Flow<String>
        get() = _notificationTriggerTimeChangeMessage.receiveAsFlow()

    init {
        edgeToEdgeEnabler.isEnabledLiveData.asFlow()
            .distinctUntilChanged()
            .onEach { edgeToEdgeValue.value = it }
            .launchIn(viewModelScope)
    }

    fun setNotificationTriggerTime(notificationTriggerTime: NotificationPrefManager.NotificationTriggerTime?) {
        viewModelScope.launch {
            if (notificationTriggerTime == null) {
                notificationPrefManager.setNotificationMessagePayload(null)
                notificationPrefManager.setNotificationTriggerTime(null)
            } else {
                getWordsInteractor.getWords(1, false)
                    .firstOrNull { it.status != Status.LOADING }
                    ?.let { resource ->
                        if (resource.status == Status.SUCCESS) {
                            resource.data?.firstOrNull()?.let { word ->
                                notificationPrefManager.setNotificationMessagePayload(
                                    FBMessageService.MessagePayload(
                                        title = "Hey There! Here's your new word '${word.word}'",
                                        body = "Tap to learn the word",
                                        noitificationType = NOTIFICATION_NEW_WORD,
                                        date = word.date.orEmpty(),
                                        deepLink = DEEP_LINK_TO_WORD_DETAILED,
                                        wordMeaning = word.meanings?.firstOrNull().orEmpty()
                                    )
                                )
                                notificationPrefManager.setNotificationTriggerTime(
                                    notificationTriggerTime
                                )
                            }
                        } else {
                            //handle error state
                        }
                    }
            }
            _notificationTriggerTimeChangeMessage.send(getSubtitleNotificationMessage(notificationTriggerTime))
        }
    }

    private fun getSubtitleNotificationMessage(triggerTime: NotificationPrefManager.NotificationTriggerTime?): String {
        notificationTriggerTime.value = triggerTime
        val time = if (triggerTime != null) {
            val cal = getLocalCalendar().apply { timeInMillis = triggerTime.timeInMillis }
            CalenderUtil.convertCalenderToString(
                cal,
                "hh:mm a"
            )
        } else {
            CalenderUtil.convertCalenderToString(
                getLocalCalendar(17, 0),
                "hh:mm a"
            )
        }
        return String.format(
            context.resources.getString(R.string.setting_notification_change_time_desc),
            time,
            if (triggerTime?.isNextDay == true) "next day" else "same day"
        )
    }

    init {
        notificationPrefManager.getNotificationTriggerTime()
            .asFlow()
            .onEach { triggerTime ->
                notificationTriggerTime.value = triggerTime
                changeNotificationSubtitle.value = getSubtitleNotificationMessage(triggerTime)
                //scheduling alarm for the notification when there is any change to time
                if (triggerTime != null) {
                    notificationAlarmScheduler.scheduleAlarm(triggerTime)
                } else {
                    notificationAlarmScheduler.cancelAlarm()
                }


            }.launchIn(viewModelScope)

        notificationPrefManager.getDailyWordNotificationEnabledLiveData()
            .asFlow()
            .onEach { isNotificationEnabled ->
                //schedule or cancel notification alarm if needed
                if (isNotificationEnabled) {
                    notificationPrefManager.getNotificationTriggerTimeNonLive()
                        ?.let { triggerTime ->
                            notificationAlarmScheduler.scheduleAlarm(triggerTime)
                        } ?: run {
                        notificationAlarmScheduler.cancelAlarm()
                    }
                } else {
                    notificationAlarmScheduler.cancelAlarm()
                }
            }.launchIn(viewModelScope)
    }

}