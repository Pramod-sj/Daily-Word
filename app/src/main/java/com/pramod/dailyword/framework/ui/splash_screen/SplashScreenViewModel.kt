package com.pramod.dailyword.framework.ui.splash_screen

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.pramod.dailyword.framework.firebase.FBTopicSubscriber
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import com.pramod.dailyword.framework.ui.common.BaseViewModel
import com.pramod.dailyword.framework.ui.notification_consent.ImportantPermissionState
import com.pramod.dailyword.framework.util.Event
import com.pramod.dialyword.games.featureCard.FeatureCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    val savedStateHandle: SavedStateHandle,
    private val prefManager: PrefManager,
    private val fbTopicSubscriber: FBTopicSubscriber,
    private val importantPermissionState: ImportantPermissionState,
    private val featureCardRepository: FeatureCardRepository
) : BaseViewModel() {
    private val animateSplashIcon = MutableLiveData<Boolean>().apply {
        value = true
    }
    private val enabledStartButton = MutableLiveData<Boolean>()
    private val splashScreenTextVisible = MutableLiveData<Boolean>()
    private val splashScreenText = MutableLiveData<String>()
    private val splashScreenSubText = MutableLiveData<String>()

    private val navigateToHomePage = MutableLiveData<Event<Boolean>>()

    private val _navigateToNotificationConsent = MutableLiveData<Event<Boolean>>()
    val navigateToNotificationConsent: LiveData<Event<Boolean>>
        get() = _navigateToNotificationConsent

    init {
        //subscribe to receive notification
        fbTopicSubscriber.subscribeToDailyWordNotification()

        //subscribe to country code
        fbTopicSubscriber.subscribeToCountry(viewModelScope)

        viewModelScope.launch {
            splashScreenTextVisible.asFlow()
                .collect {
                    if (prefManager.isNewUser()) {
                        startSplashSequence()
                    } else {
                        loadDataAndNavigate()
                    }
                }
        }
    }

    private suspend fun startSplashSequence() {
        // 1. Initial State
        splashScreenText.value = "Hi, There!"
        // 2. Wait 2 seconds
        delay(2000)
        // 3. Update text
        splashScreenText.value = "Welcome to Daily Word"
        // 4. Wait 1 second
        delay(1000)
        // 5. Final State
        splashScreenSubText.value = "Learn a new word every day!"
        enabledStartButton.value = true
    }

    private suspend fun loadDataAndNavigate() = coroutineScope {
        // 1. Fire off the API call and the Timer at the EXACT same time
        val apiTask = async(Dispatchers.Default) {
            featureCardRepository.fetchLiveFeatures() // Your suspend API call
        }
        val timerTask = async {
            delay(500) // Your minimum 500ms wait
        }
        // 2. Wait for BOTH of them to finish
        apiTask.await()
        timerTask.await()
        // 3. Navigate!
        goToHomePage()
    }

    val isNewUser: Boolean = prefManager.isNewUser()


    fun animateSplashIcon(): LiveData<Boolean> = animateSplashIcon
    fun splashScreenText(): LiveData<String> = splashScreenText
    fun splashScreenSubText(): LiveData<String> = splashScreenSubText
    fun splashScreenTextVisible(): LiveData<Boolean> = splashScreenTextVisible
    fun enableStartButton(): LiveData<Boolean> = enabledStartButton
    fun navigateToHomePage(): LiveData<Event<Boolean>> = navigateToHomePage


    fun goToHomePage() {
        prefManager.markUserAsOld()
        if (importantPermissionState.isNotificationEnabled.value) {
            navigateToHomePage.value = Event.init(true)
        } else {
            if (importantPermissionState.canShowFullNotificationEnableMessage.value == true) {
                _navigateToNotificationConsent.value = Event.init(true)
            } else {
                navigateToHomePage.value = Event.init(true)
            }
        }
    }

    fun showSplashText() {
        splashScreenTextVisible.value = true
    }
}