package com.pramod.dailyword.ui.splash_screen

import android.app.Application
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.pramod.dailyword.helper.PrefManager
import com.pramod.dailyword.ui.BaseViewModel
import com.pramod.dailyword.util.Event

class SplashScreenViewModel(application: Application) : BaseViewModel(application) {
    private val prefManager = PrefManager(application)
    private val animateSplashIcon = MutableLiveData<Boolean>().apply {
        value = prefManager.isNewUser()
    }
    private val enabledStartButton = MutableLiveData<Boolean>()
    private val splashScreenTextVisible = MutableLiveData<Boolean>()
    private val splashScreenText = MutableLiveData<String>()
    private val splashScreenSubText = MutableLiveData<String>()

    private val navigateToHomePage = MutableLiveData<Event<Boolean>>()

    init {

        splashScreenTextVisible.observeForever(object : Observer<Boolean> {
            override fun onChanged(t: Boolean?) {
                splashScreenTextVisible.removeObserver(this)
                if (prefManager.isNewUser()) {
                    splashScreenText.value = "Hi, There!"
                    Handler().postDelayed({
                        splashScreenText.postValue("Welcome to Daily Word")
                        Handler().postDelayed(
                            {
                                splashScreenSubText.postValue("Learn a new word every day!")
                                enabledStartButton.postValue(true)
                            }, 1000
                        )
                    }, 2000)
                } else {
                    Handler().postDelayed({
                        goToHomePage()
                    }, 1000)
                }
            }
        })
    }


    fun animateSplashIcon(): LiveData<Boolean> = animateSplashIcon
    fun splashScreenText(): LiveData<String> = splashScreenText
    fun splashScreenSubText(): LiveData<String> = splashScreenSubText
    fun splashScreenTextVisible(): LiveData<Boolean> = splashScreenTextVisible
    fun enableStartButton(): LiveData<Boolean> = enabledStartButton
    fun navigateToHomePage(): LiveData<Event<Boolean>> = navigateToHomePage


    fun goToHomePage() {
        prefManager.setIsNewUser(false)
        navigateToHomePage.value = Event.init(true)
    }

    fun showSplashText() {
        splashScreenTextVisible.value = true
    }
}