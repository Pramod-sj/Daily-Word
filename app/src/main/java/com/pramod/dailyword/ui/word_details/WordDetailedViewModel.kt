package com.pramod.dailyword.ui.word_details

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.pramod.dailyword.SnackbarMessage
import com.pramod.dailyword.db.Resource
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.db.repository.BookmarkRepo
import com.pramod.dailyword.db.repository.WOTDRepository
import com.pramod.dailyword.helper.PronounceHelper
import com.pramod.dailyword.ui.BaseViewModel
import com.pramod.dailyword.util.CommonUtils
import com.pramod.dailyword.util.Event
import kotlinx.coroutines.*

class WordDetailedViewModel(
    application: Application,
    private var wordDate: String?
) : BaseViewModel(application) {

    companion object {
        val TAG = WordDetailedViewModel::class.simpleName
    }

    //this will be false when user open this activity first
    //when user manually pull to refresh this will be change to true
    private var shouldForceRefresh = wordDate?.let { false } ?: true

    private val bookmarkRepo = BookmarkRepo(application)
    private val wordOfTheDayRepo = WOTDRepository(application)

    private val retryEventLiveData = MutableLiveData<Boolean>()

    val loadingLiveData = MutableLiveData<Boolean>()

    var navigator: WordDetailNavigator? = null

    fun retry() {
        shouldForceRefresh = true
        retryEventLiveData.value = true
    }

    var wordOfTheDayLiveData: LiveData<WordOfTheDay?> = MutableLiveData<WordOfTheDay?>()

    init {
        val wordOfTheDayLiveDataResource: LiveData<Resource<WordOfTheDay?>> =
            retryEventLiveData.switchMap {
                return@switchMap if (wordDate == null) {
                    wordOfTheDayLiveData.value?.let { word ->
                        wordOfTheDayRepo.getWord(
                            word.date!!, true
                        )
                    } ?: wordOfTheDayRepo.getRandomWord()
                } else wordOfTheDayRepo.getWord(wordDate!!, shouldForceRefresh)
            }

        wordOfTheDayLiveData = wordOfTheDayLiveDataResource.map { resource ->
            loadingLiveData.value = resource.status == Resource.Status.LOADING
            Log.i(TAG, ": ${resource.status}")
            if (resource.status == Resource.Status.ERROR) {
                setMessage(SnackbarMessage.init(resource.message ?: "Something went wrong!!"))
            }
            return@map resource.data
        }

        retryEventLiveData.value = true

    }

    private val showTitle = MutableLiveData<Boolean>().apply {
        value = false
    }

    private val navigateToMerriamWebster = MutableLiveData<Event<String>>()


    fun navigateToWordMW(url: String) {
        navigateToMerriamWebster.value = Event.init(url)
    }

    fun setTitleVisibility(show: Boolean) {
        showTitle.value = show
    }

    fun showTitle(): LiveData<Boolean> = showTitle

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

    fun navigateToMerriamWebster(): LiveData<Event<String>> = navigateToMerriamWebster

    fun bookmark() {
        getCoroutineScope().launch(Dispatchers.Main) {
            val wordOfTheDay = wordOfTheDayLiveData.value
            if (wordOfTheDay != null) {
                bookmarkRepo.bookmarkToggle(wordOfTheDay.word!!)
                Log.i(TAG, "bookmark: not null")
            } else {
                setMessage(SnackbarMessage.init("Word details is not loaded yet! Please wait..."))
                Log.i(TAG, "bookmark: Word details is not loaded yet! Please wait...")
            }
        }
    }

    class Factory(
        private val application: Application,
        private val wordDate: String?
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return WordDetailedViewModel(application, wordDate) as T
        }

    }

    fun copyWordToClipboard(word: String) {
        CommonUtils.copyToClipboard(getApplication(), word)
        setMessage(SnackbarMessage.init("Copied"))
    }
}