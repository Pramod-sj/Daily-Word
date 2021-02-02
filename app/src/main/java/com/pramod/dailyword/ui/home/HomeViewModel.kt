package com.pramod.dailyword.ui.home

import android.app.Application
import android.text.SpannableString
import android.util.Log
import androidx.lifecycle.*
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.SnackbarMessage
import com.pramod.dailyword.db.Resource
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.db.repository.WOTDRepository
import com.pramod.dailyword.helper.PrefManager
import com.pramod.dailyword.helper.PronounceHelper
import com.pramod.dailyword.ui.BaseViewModel
import com.pramod.dailyword.util.CommonUtils
import com.pramod.dailyword.util.Event
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : BaseViewModel(application) {
    private val wordOfTheDayRepo: WOTDRepository =
        WOTDRepository(application)

    private val title = MutableLiveData<SpannableString>()

    fun title(): LiveData<SpannableString> = title

    fun setTitle(text: SpannableString) {
        title.value = text

    }

    private val _showLoading = MutableLiveData<Boolean>()

    val showLoading: LiveData<Boolean>
        get() = _showLoading

    private val refreshDataSourceLiveData: MutableLiveData<Boolean> = MutableLiveData()

    private var wordOfTheDayLiveData: LiveData<WordOfTheDay?>
    private var wordsExceptTodayLiveData: LiveData<List<WordOfTheDay>?>

    var firstNotificationShown = false


    init {
        val wordResourceLiveData: LiveData<Resource<List<WordOfTheDay>?>> =
            refreshDataSourceLiveData.switchMap {
                wordOfTheDayRepo.getWords()
            }

        val wordList = MutableLiveData<List<WordOfTheDay>?>()

        wordResourceLiveData.asFlow().onEach { }
            .onEach {
                Log.i("TAG", "wordResourceLiveData: ${it.status.name}")
                _showLoading.value = it.status == Resource.Status.LOADING
                if (it.status == Resource.Status.ERROR) {
                    it.message?.let { message ->
                        setMessage(SnackbarMessage.init(message))
                    }
                }
                wordList.value = it.data
            }.launchIn(viewModelScope)


        wordOfTheDayLiveData = wordList.map {
            if (it != null && it.isNotEmpty()) it[0] else null
        }

        wordsExceptTodayLiveData = wordList.map {
            val list = it?.toMutableList()
            if (list != null && list.isNotEmpty()) {
                list.removeAt(0)
            }
            return@map list
        }

        refreshDataSource()
    }

    fun refreshDataSource() {
        refreshDataSourceLiveData.value = true
    }

    fun getTodaysWordOfTheDay(): LiveData<WordOfTheDay?> {
        return wordOfTheDayLiveData
    }


    fun getWordsExceptToday(): LiveData<List<WordOfTheDay>?> {
        return wordsExceptTodayLiveData
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
                _isAudioPronouncing.value =false
            }
        }
    }

    fun updateWordSeenStatus(word: WordOfTheDay) {
        viewModelScope.launch {
            wordOfTheDayRepo.updateWord(word)
        }
    }

    val showChangelogActivity: LiveData<Event<Boolean>> = MutableLiveData<Event<Boolean>>().apply {
        val prefManager = PrefManager(application)
        value = if (prefManager.getLastAppVersion() < BuildConfig.VERSION_CODE) {
            prefManager.updateAppVersion()
            Event.init(true)
        } else {
            Event.init(false)
        }
    }

    fun copyWordToClipboard(word: String) {
        CommonUtils.copyToClipboard(getApplication(), word)
        setMessage(SnackbarMessage.init("Copied"))
    }
}