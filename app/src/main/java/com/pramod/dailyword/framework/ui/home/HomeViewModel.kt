package com.pramod.dailyword.framework.ui.home

import android.text.SpannableString
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.library.audioplayer.AudioPlayer
import com.pramod.dailyword.business.data.network.Status
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.business.interactor.GetWordsInteractor
import com.pramod.dailyword.business.interactor.MarkWordAsSeenInteractor
import com.pramod.dailyword.framework.haptics.HapticFeedbackManager
import com.pramod.dailyword.framework.haptics.HapticType
import com.pramod.dailyword.framework.helper.ads.InterstitialAdTracker
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import com.pramod.dailyword.framework.ui.common.BaseViewModel
import com.pramod.dailyword.framework.ui.common.Message
import com.pramod.dailyword.framework.ui.notification_consent.ImportantPermissionState
import com.pramod.dailyword.framework.util.CalenderUtil
import com.pramod.dailyword.framework.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getWordsInteractor: GetWordsInteractor,
    private val markWordAsSeenInteractor: MarkWordAsSeenInteractor,
    private val prefManager: PrefManager,
    val audioPlayer: AudioPlayer,
    private val importantPermissionState: ImportantPermissionState,
    private val interstitialAdTracker: InterstitialAdTracker,
    private val hapticFeedbackManager: HapticFeedbackManager
) : BaseViewModel() {

    companion object {
        val TAG = HomeViewModel::class.java.simpleName
    }

    private val _canShowSettingIssueWarningMessage = MutableLiveData<Boolean>()
    val canShowSettingIssueWarningMessage: LiveData<Boolean>
        get() = _canShowSettingIssueWarningMessage

    private fun settingIssueWarningMessage() {
        combine(
            importantPermissionState.isSetAlarmEnabled,
            importantPermissionState.isNotificationEnabled,
            importantPermissionState.isBatteryOptimizationDisabled,
            importantPermissionState.isUnusedAppPausingDisabled,
            prefManager.isSettingIssueWarningMessageDismissed().asFlow()
        ) { s, n, b, unused, isCardDismissed ->
            _canShowSettingIssueWarningMessage.value =
                !isCardDismissed && (!s || !n || !b || !unused)
        }.launchIn(viewModelScope)

    }


    private val _navigateToTroubleshootScreen = MutableLiveData<Event<Unit>>()
    val navigateToTroubleshootScreen: LiveData<Event<Unit>>
        get() = _navigateToTroubleshootScreen

    private val _navigateToBatteryOptimizationPage = MutableLiveData<Event<Unit>>()
    val navigateToBatteryOptimizationPage: LiveData<Event<Unit>>
        get() = _navigateToBatteryOptimizationPage


    private val title = MutableLiveData<SpannableString>()

    fun title(): LiveData<SpannableString> = title

    fun setTitle(text: SpannableString) {
        title.value = text
    }

    private val _showLoading = MutableLiveData<Boolean>()

    val showLoading: LiveData<Boolean>
        get() = _showLoading

    private val _refreshEvent = MutableLiveData<Unit>().apply {
        //for initial load
        value = Unit
    }

    val wordOfTheDay: LiveData<Word?>

    private val _last6DayWords = MediatorLiveData<List<PastWordUIModel>?>()
    val last6DayWords: LiveData<List<PastWordUIModel>?>
        get() = _last6DayWords

    var firstNotificationShown = false

    var navigator: HomeNavigator? = null

    init {

        val wordList: LiveData<List<Word>?> = _refreshEvent.switchMap {
            return@switchMap getWordsInteractor.getWords(7).asLiveData(Dispatchers.IO)
        }.map { resource ->
            _showLoading.value = resource.status == Status.LOADING
            if (resource.status == Status.ERROR) {
                resource.error?.message?.let { message ->
                    setMessage(Message.SnackBarMessage(message))
                }
            }
            return@map resource.data
        }

        wordOfTheDay = wordList.map {
            if (it?.isNotEmpty() == true) it[0]
            else null
        }

        val last6DayWordsSource1 = wordList.map {
            it?.let { list ->
                val newList = list.toMutableList().apply {
                    if (isNotEmpty()) {
                        removeAt(0)
                    }
                }
                newList
            }
        }
        val last6DayWordsSource2 = prefManager.getHideBadgeLiveData()
        _last6DayWords.addSource(last6DayWordsSource1) {
            _last6DayWords.value = it?.map { word ->
                PastWordUIModel(
                    CalenderUtil.getFancyDay(word.date, CalenderUtil.DATE_FORMAT),
                    word,
                    !word.isSeen && last6DayWordsSource2.value == false
                )
            }
        }
        _last6DayWords.addSource(last6DayWordsSource2) {
            _last6DayWords.value = last6DayWordsSource1.value?.map { word ->
                PastWordUIModel(
                    CalenderUtil.getFancyDay(word.date, CalenderUtil.DATE_FORMAT),
                    word,
                    !word.isSeen && it == false
                )
            }
        }

        settingIssueWarningMessage()
    }

    fun refresh() {
        _refreshEvent.value = Unit
    }


    fun updateWordSeenStatus(word: Word) {
        markWordAsSeenInteractor.markAsSeen(word.word)
    }


    data class AppUpdateModel(
        val message: SpannableString?,
        val downloadPercentage: Int = 0,
        val buttonText: String = "Update"
    )

    val appUpdateModel = MutableLiveData<AppUpdateModel?>(null)

    fun setAppUpdateModel(appUpdateModel: AppUpdateModel?) {
        this.appUpdateModel.value = appUpdateModel
    }

    fun setAppUpdateMessage(message: SpannableString) {
        val current = appUpdateModel.value
        this.appUpdateModel.value =
            current?.copy(message = message) ?: AppUpdateModel(message = message)
    }

    fun setAppUpdateDownloadProgress(downloadProgress: Int) {
        val current = appUpdateModel.value
        appUpdateModel.value =
            current?.copy(downloadPercentage = downloadProgress)
    }

    fun setAppUpdateButtonText(buttonText: String) {
        val current = appUpdateModel.value
        appUpdateModel.value = current?.copy(buttonText = buttonText)
    }

    fun enableNotificationNeverClick() {
        importantPermissionState.markSettingIssueMessageDismissed()
    }

    fun fixSettingIssuesClick() {
        _navigateToTroubleshootScreen.value = Event.init(Unit)
    }

    fun navigateToBatteryOptimizationPage() {
        _navigateToBatteryOptimizationPage.value = Event.init(Unit)
    }

    private var job: Job? = null
    fun playAudio(url: String) {
        hapticFeedbackManager.perform(HapticType.CLICK)
        audioPlayer.play(url)
        job?.cancel()
        job = viewModelScope.launch {
            audioPlayer.audioPlaying.asFlow()
                .firstOrNull { !it.peekContent() } //wait till audio plays
            delay(500L)
            interstitialAdTracker.incrementActionCount()
        }
    }

}