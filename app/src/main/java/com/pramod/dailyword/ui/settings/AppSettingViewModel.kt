package com.pramod.dailyword.ui.settings

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pramod.dailyword.SnackbarMessage
import com.pramod.dailyword.firebase.FBTopicSubscriber
import com.pramod.dailyword.helper.*
import com.pramod.dailyword.helper.WindowPrefManager
import com.pramod.dailyword.ui.BaseViewModel
import com.pramod.dailyword.util.Event
import com.pramod.dailyword.util.NetworkUtils

class AppSettingViewModel(application: Application) : BaseViewModel(application) {
    private val prefManager = PrefManager.getInstance(application)

    val windowPrefManager = WindowPrefManager.newInstance(application)

    val notificationPrefManager = NotificationPrefManager.newInstance(application)

    val themeManager = ThemeManager.newInstance(application)

    val windowAnimationPrefManager = WindowAnimationPrefManager.newInstance(application)

    private val showThemeSelector = MutableLiveData<Event<ThemeManager.Options>>()

    private val navigateToAbout = MutableLiveData<Event<Boolean>>()

    private val recreateActivity = MutableLiveData<Event<Boolean>>()

    fun showThemeSelector() {
        showThemeSelector.value = Event.init(themeManager.getDefaultThemeModeOption())
    }

    fun changeThemePref(option: ThemeManager.Options) {
        themeManager.setDefaultThemeMode(option)
    }

    fun applyTheme(option: ThemeManager.Options) {
        themeManager.applyTheme(option);
    }


    fun toggleEdgeToEdge() {
        windowPrefManager.toggleEdgeToEdgeEnabled()
        recreate()
    }

    private val toggleNotificationMessageLiveData = MutableLiveData<String>().apply {
        value =
            if (notificationPrefManager.isNotificationEnabled())
                "You'll be receiving daily notifications"
            else "You have paused daily notifications"
    }

    fun toggleNotificationMessageLiveData(): LiveData<String> =
        toggleNotificationMessageLiveData

    private val toggleNotificationClickableLiveData = MutableLiveData<Boolean>().apply {
        value = true
    }

    fun toggleNotificationClickableLiveData(): LiveData<Boolean> =
        toggleNotificationClickableLiveData

    fun toggleNotification() {
        if (NetworkUtils.isNetworkActive(getApplication())) {
            toggleNotificationClickableLiveData.value = false
            toggleNotificationMessageLiveData.value = "Please wait..."
            notificationPrefManager.toggleNotificationEnabled { s, operationStatus ->
                if (operationStatus == FBTopicSubscriber.OperationStatus.FAILED) {
                    setMessage(SnackbarMessage.init(s))
                }
                toggleNotificationClickableLiveData.value = true
                toggleNotificationMessageLiveData.value =
                    if (notificationPrefManager.isNotificationEnabled())
                        "You'll be receiving daily notifications"
                    else "You have paused daily notifications"
            }
        } else {
            setMessage(SnackbarMessage.init("Please enable your internet!"))
        }
    }


    fun toggleWindowAnimation() {
        windowAnimationPrefManager.toggleWindowAnimationEnabled()
    }

    fun goToAbout() {
        navigateToAbout.value = Event.init(true)
    }

    fun recreate() {
        recreateActivity.value = Event.init(true)
    }

    fun getShowThemeSelector(): LiveData<Event<ThemeManager.Options>> = showThemeSelector
    fun navigateToAbout(): LiveData<Event<Boolean>> = navigateToAbout
    fun recreateActivity(): LiveData<Event<Boolean>> = recreateActivity
}