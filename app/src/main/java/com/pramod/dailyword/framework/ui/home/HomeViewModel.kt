package com.pramod.dailyword.framework.ui.home

import android.text.SpannableString
import androidx.lifecycle.*
import com.library.audioplayer.AudioPlayer
import com.pramod.dailyword.business.data.network.Status
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.business.interactor.GetWordsInteractor
import com.pramod.dailyword.business.interactor.MarkWordAsSeenInteractor
import com.pramod.dailyword.framework.prefmanagers.HomeScreenBadgeManager
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import com.pramod.dailyword.framework.ui.common.BaseViewModel
import com.pramod.dailyword.framework.ui.common.Message
import com.pramod.dailyword.framework.util.CalenderUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getWordsInteractor: GetWordsInteractor,
    private val markWordAsSeenInteractor: MarkWordAsSeenInteractor,
    private val badgeManager: HomeScreenBadgeManager,
    private val prefManager: PrefManager,
    val audioPlayer: AudioPlayer
) : BaseViewModel() {

    companion object {
        val TAG = HomeViewModel::class.java.simpleName
    }

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
            AppUpdateModel(message, current?.downloadPercentage ?: 0)
    }

    fun setAppUpdateDownloadProgress(downloadProgress: Int) {
        val current = appUpdateModel.value
        appUpdateModel.value = AppUpdateModel(current?.message, downloadProgress)
    }

    fun setAppUpdateButtonText(buttonText: String) {
        val current = appUpdateModel.value
        appUpdateModel.value =
            AppUpdateModel(current?.message, current?.downloadPercentage ?: 0, buttonText)
    }

}