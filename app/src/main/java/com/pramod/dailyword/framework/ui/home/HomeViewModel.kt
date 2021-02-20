package com.pramod.dailyword.framework.ui.home

import android.text.SpannableString
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.business.data.network.Resource
import com.pramod.dailyword.business.data.network.Status
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.business.interactor.GetWordsInteractor
import com.pramod.dailyword.business.interactor.MarkWordAsSeenInteractor
import com.pramod.dailyword.framework.ui.common.BaseViewModel
import com.pramod.dailyword.framework.ui.common.Message
import com.pramod.dailyword.framework.helper.PronounceHelper
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val prefManager: PrefManager,
    private val getWordsInteractor: GetWordsInteractor,
    private val markWordAsSeenInteractor: MarkWordAsSeenInteractor
) : BaseViewModel() {

    private val title = MutableLiveData<SpannableString>()

    fun title(): LiveData<SpannableString> = title

    fun setTitle(text: SpannableString) {
        title.value = text
    }

    private val _showLoading = MutableLiveData<Boolean>()

    val showLoading: LiveData<Boolean>
        get() = _showLoading

    private val _refreshEvent = MutableStateFlow(true)

    private var _wordOfTheDayLiveData = MutableLiveData<Word?>()

    val wordOfTheDayLiveData: LiveData<Word?>
        get() = _wordOfTheDayLiveData


    private var _wordsExceptTodayLiveData = MutableLiveData<List<Word>?>()

    val wordsExceptTodayLiveData: LiveData<List<Word>?>
        get() = _wordsExceptTodayLiveData

    var firstNotificationShown = false

    var navigator: HomeNavigator? = null


    init {

        val wordList: Flow<Resource<List<Word>?>> = _refreshEvent.transformLatest {
            Log.i("TAG", ": " + _refreshEvent.value)
            if (_refreshEvent.value) {
                emitAll(getWordsInteractor.getWords(7))
            }
        }

        wordList.onEach { resource ->
            Log.i("TAG", ": " + Gson().toJson(resource))
            _showLoading.value = resource.status == Status.LOADING
            if (resource.status == Status.ERROR) {
                resource.error?.message?.let { message ->
                    setMessage(Message.SnackBarMessage(message))
                }
            }
            resource.data?.let {
                _wordOfTheDayLiveData.value =
                    if (it.isNotEmpty()) it[0] else null

                val newList = it.toMutableList().apply {
                    if (isNotEmpty()) {
                        removeAt(0)
                    }
                }
                _wordsExceptTodayLiveData.value = newList

            }

        }.launchIn(viewModelScope)
    }

    fun refresh() {
        _refreshEvent.value = true
    }


    private var _isAudioPronouncing = MutableLiveData<Boolean>().apply {
        value = false
    }

    val isAudioPronouncing: LiveData<Boolean>
        get() = _isAudioPronouncing

    fun pronounceWord(url: String) {
        Log.d("AUDIO URL", url)
        if (_isAudioPronouncing.value == false) {
            _isAudioPronouncing.value = true
            PronounceHelper.playAudio(url) {
                _isAudioPronouncing.value = false
            }
        }
    }

    fun updateWordSeenStatus(word: Word) {
        markWordAsSeenInteractor.markAsSeen(word)
    }

    val showChangelogActivity: Flow<Boolean> = flow {
        if (prefManager.getLastAppVersion() < BuildConfig.VERSION_CODE) {
            prefManager.updateAppVersion()
            emit(true)
        } else {
            emit(false)
        }

    }

}