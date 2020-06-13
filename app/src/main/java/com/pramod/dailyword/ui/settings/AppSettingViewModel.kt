package com.pramod.dailyword.ui.settings

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pramod.dailyword.helper.NotificationPrefManager
import com.pramod.dailyword.helper.PrefManager
import com.pramod.dailyword.helper.ThemeManager
import com.pramod.dailyword.helper.WindowPreferencesManager
import com.pramod.dailyword.ui.BaseViewModel
import com.pramod.dailyword.util.Event

class AppSettingViewModel(application: Application) : BaseViewModel(application) {
    private val prefManager = PrefManager.getInstance(application)

    val windowPrefManager = WindowPreferencesManager.newInstance(application)

    val notificationPrefManager = NotificationPrefManager.newInstance(application)

    private val themeOption = MutableLiveData<String>().apply {
        value = ThemeManager.getDefaultThemeOption().name
    }

    private val showThemeSelector = MutableLiveData<Event<ThemeManager.Options>>()

    private val navigateToAbout = MutableLiveData<Event<Boolean>>()


    private val recreateActivity = MutableLiveData<Event<Boolean>>()

    fun showThemeSelector() {
        showThemeSelector.value = Event.init(ThemeManager.getDefaultThemeOption())
    }

    fun changeThemePref(option: ThemeManager.Options) {
        ThemeManager.applyTheme(option)
        themeOption.value = option.name
    }

    fun toggleEdgeToEdge() {
        windowPrefManager.toggleEdgeToEdgeEnabled()
        recreate()
    }

    fun toggleNotification() {
        notificationPrefManager.toggleNotificationEnabled()
    }

    fun goToAbout() {
        navigateToAbout.value = Event.init(true)
    }

    fun recreate() {
        recreateActivity.value = Event.init(true)
    }

    fun themeOption(): LiveData<String> = themeOption
    fun getShowThemeSelector(): LiveData<Event<ThemeManager.Options>> = showThemeSelector
    fun navigateToAbout(): LiveData<Event<Boolean>> = navigateToAbout
    fun recreateActivity(): LiveData<Event<Boolean>> = recreateActivity
}