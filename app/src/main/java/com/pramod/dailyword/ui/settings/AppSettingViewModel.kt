package com.pramod.dailyword.ui.settings

import android.app.Application
import android.content.ClipboardManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.messaging.FirebaseMessaging
import com.pramod.dailyword.SnackbarMessage
import com.pramod.dailyword.firebase.FBTopicSubscriber
import com.pramod.dailyword.helper.*
import com.pramod.dailyword.helper.WindowPrefManager
import com.pramod.dailyword.ui.BaseViewModel
import com.pramod.dailyword.util.CommonUtils
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

    fun copyFirebaseTokenId() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                CommonUtils.copyToClipboard(getApplication(), it.result)
                setMessage(SnackbarMessage.init("Your token has been captured!"))
            } else {
                setMessage(SnackbarMessage.init("Sorry we're currently not able to fetch your token"))
            }
        }
    }


    fun getShowThemeSelector(): LiveData<Event<ThemeManager.Options>> = showThemeSelector
    fun navigateToAbout(): LiveData<Event<Boolean>> = navigateToAbout
    fun recreateActivity(): LiveData<Event<Boolean>> = recreateActivity
    fun navigateToDokiActivity(): LiveData<Event<Boolean>> = navigateToDokiActivity
}