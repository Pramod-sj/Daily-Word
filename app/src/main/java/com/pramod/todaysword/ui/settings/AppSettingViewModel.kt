package com.pramod.todaysword.ui.settings

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.pramod.todaysword.helper.PrefManager
import com.pramod.todaysword.helper.ThemeManager
import com.pramod.todaysword.ui.BaseViewModel
import com.pramod.todaysword.util.Event

class AppSettingViewModel(application: Application) : BaseViewModel(application) {
    private val prefManager = PrefManager.getInstance(application)
    private val themeOption = MutableLiveData<String>().apply {
        value = ThemeManager.getDefaultThemeOption().name
    }
    private val enableNotification = MutableLiveData<Event<Boolean>>().apply {
        value = Event.init(prefManager.isNotificationEnabled())
    }
    private val showThemeSelector = MutableLiveData<Event<ThemeManager.Options>>()

    private val coloredNavBar = MutableLiveData<Event<Boolean>>().apply {
        value = Event.init(prefManager.isColoredNavBarEnabled())
    }

    private val navigateToAbout = MutableLiveData<Event<Boolean>>()


    fun showThemeSelector() {
        showThemeSelector.value = Event.init(ThemeManager.getDefaultThemeOption())
    }

    fun changeThemePref(option: ThemeManager.Options) {
        ThemeManager.applyTheme(option)
        themeOption.value = option.name
    }

    fun changeNotificationEnable(enable: Boolean) {
        enableNotification.value = Event.init(enable)
        prefManager.setNotificationEnabled(enable)
    }


    fun changeColoredNavBarEnable(enable: Boolean) {
        coloredNavBar.value = Event.init(enable)
        prefManager.setColorNavBarEnabled(enable)
    }

    fun goToAbout() {
        navigateToAbout.value = Event.init(true)
    }


    fun themeOption(): LiveData<String> = themeOption
    fun getShowThemeSelector(): LiveData<Event<ThemeManager.Options>> = showThemeSelector
    fun getNotificationEnabled(): LiveData<Event<Boolean>> = enableNotification
    fun getColoredNavBarEnabled(): LiveData<Event<Boolean>> = coloredNavBar
    fun navigateToAbout(): LiveData<Event<Boolean>> = navigateToAbout

}