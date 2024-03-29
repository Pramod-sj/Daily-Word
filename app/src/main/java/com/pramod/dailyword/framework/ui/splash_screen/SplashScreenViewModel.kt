package com.pramod.dailyword.framework.ui.splash_screen

import android.annotation.SuppressLint
import android.os.Handler
import androidx.lifecycle.*
import com.pramod.dailyword.framework.firebase.FBTopicSubscriber
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import com.pramod.dailyword.framework.ui.common.BaseViewModel
import com.pramod.dailyword.framework.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    val savedStateHandle: SavedStateHandle,
    private val prefManager: PrefManager,
    private val fbTopicSubscriber: FBTopicSubscriber
) : BaseViewModel() {
    private val animateSplashIcon = MutableLiveData<Boolean>().apply {
        value = true
    }
    private val enabledStartButton = MutableLiveData<Boolean>()
    private val splashScreenTextVisible = MutableLiveData<Boolean>()
    private val splashScreenText = MutableLiveData<String>()
    private val splashScreenSubText = MutableLiveData<String>()

    private val navigateToHomePage = MutableLiveData<Event<Boolean>>()

    init {
        //subscribe to receive notification
        fbTopicSubscriber.subscribeToDailyWordNotification()

        //subscribe to country code
        fbTopicSubscriber.subscribeToCountry(viewModelScope)


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
                    }, 500)
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
        prefManager.markUserAsOld()
        navigateToHomePage.value = Event.init(true)
    }

    fun showSplashText() {
        splashScreenTextVisible.value = true
    }
}