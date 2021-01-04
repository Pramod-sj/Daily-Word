package com.pramod.dailyword.ui.word_details

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
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
    private var wordDate: String?,
    private val showRandomWord: Boolean
) : BaseViewModel(application) {

    companion object {
        val TAG = WordDetailedViewModel::class.simpleName
    }

    private val bookmarkRepo = BookmarkRepo(application)
    private val wordOfTheDayRepo = WOTDRepository(application)

    private val retryEventLiveData = MutableLiveData<Event<Boolean>>()

    val loadingLiveData = MutableLiveData<Boolean>()

    fun retry() {
        retryEventLiveData.value = Event.init(true)
    }

    var wordOfTheDayLiveData: LiveData<WordOfTheDay?> = MutableLiveData<WordOfTheDay?>()

    init {

        val wordOfTheDayLiveDataResource: LiveData<Resource<WordOfTheDay?>> =
            Transformations.switchMap(retryEventLiveData) {
                if (it.getContentIfNotHandled() == true) {
                    return@switchMap if (showRandomWord)
                        wordOfTheDayLiveData.value?.let { word ->
                            wordOfTheDayRepo.getWord(
                                word.date!!, true
                            )
                        } ?: wordOfTheDayRepo.getRandomWord()
                    else wordOfTheDayRepo.getWord(wordDate!!)
                }
                return@switchMap null
            }

        wordOfTheDayLiveData = Transformations.map(wordOfTheDayLiveDataResource) { resource ->
            loadingLiveData.value = resource.status == Resource.Status.LOADING
            if (resource.status == Resource.Status.ERROR) {
                setMessage(SnackbarMessage.init(resource.message ?: "Something went wrong!!"))
            }
            return@map resource.data
        }

        retry()

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

    private var isWordPronounced = true
    fun pronounceWord(url: String) {
        if (isWordPronounced) {
            isWordPronounced = false
            PronounceHelper.playAudio(url) {
                isWordPronounced = true
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
        private val wordDate: String?,
        private val showRandomWord: Boolean
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return WordDetailedViewModel(application, wordDate, showRandomWord) as T
        }

    }

    fun copyWordToClipboard(word: String) {
        CommonUtils.copyToClipboard(getApplication(), word)
        setMessage(SnackbarMessage.init("Copied"))
    }
}