package com.pramod.dailyword.framework.ui.home

import android.text.SpannableString
import android.util.Log
import androidx.lifecycle.*
import com.library.audioplayer.AudioPlayer
import com.pramod.dailyword.BuildConfig
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
    private val prefManager: PrefManager,
    private val getWordsInteractor: GetWordsInteractor,
    private val markWordAsSeenInteractor: MarkWordAsSeenInteractor,
    val audioPlayer: AudioPlayer,
    val homeScreenBadgeManager: HomeScreenBadgeManager
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

    val last6DayWords: LiveData<List<PastWordUIModel>?>

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

        last6DayWords = wordList.map {
            it?.let { list ->
                val newList = list.toMutableList().apply {
                    if (isNotEmpty()) {
                        removeAt(0)
                    }
                }
                newList
            }
        }.map {
            it?.map { word ->
                PastWordUIModel(CalenderUtil.getFancyDay(word.date, CalenderUtil.DATE_FORMAT), word)
            }
        }

    }

    fun refresh() {
        _refreshEvent.value = Unit
    }


    fun updateWordSeenStatus(word: Word) {
        markWordAsSeenInteractor.markAsSeen(word.word)
    }

    val showChangelogActivity: Flow<Boolean> = flow {
        Log.i(TAG, ": " + prefManager.getLastAppVersion())
        if (prefManager.getLastAppVersion() < BuildConfig.VERSION_CODE) {
            prefManager.updateAppVersion()
            emit(true)
        } else {
            emit(false)
        }

    }

}