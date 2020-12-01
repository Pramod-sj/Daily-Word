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

    private val navigateToDokiActivity = MutableLiveData<Event<Boolean>>()

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


    fun toggleWindowAnimation() {
        windowAnimationPrefManager.toggleWindowAnimationEnabled()
    }

    fun goToAbout() {
        navigateToAbout.value = Event.init(true)
    }

    fun recreate() {
        recreateActivity.value = Event.init(true)
    }

    fun goToDokiActivity() {
        navigateToDokiActivity.value = Event.init(true)
    }

    fun getShowThemeSelector(): LiveData<Event<ThemeManager.Options>> = showThemeSelector
    fun navigateToAbout(): LiveData<Event<Boolean>> = navigateToAbout
    fun recreateActivity(): LiveData<Event<Boolean>> = recreateActivity
    fun navigateToDokiActivity(): LiveData<Event<Boolean>> = navigateToDokiActivity
}