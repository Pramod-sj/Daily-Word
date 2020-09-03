package com.pramod.dailyword.ui.word_details

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.db.repository.BookmarkRepo
import com.pramod.dailyword.db.repository.WOTDRepository
import com.pramod.dailyword.ui.BaseViewModel
import com.pramod.dailyword.util.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class WordDetailedViewModel(application: Application, val wordOfTheDay: WordOfTheDay) :
    BaseViewModel(application) {
    private val bookmarkRepo = BookmarkRepo(application)
    private val wordOfTheDayRepo = WOTDRepository(application)

    private val showTitle = MutableLiveData<Boolean>().apply {
        value = false
    }

    private val navigateToMerriamWebster = MutableLiveData<Event<String>>()

    val wordOfTheDayLiveData: LiveData<WordOfTheDay>

    init {
        wordOfTheDayLiveData = wordOfTheDayRepo.getWord(wordOfTheDay.word!!)
    }


    fun navigateToWordMW(url: String) {
        navigateToMerriamWebster.value = Event.init(url)
    }

    fun setTitleVisibility(show: Boolean) {
        showTitle.value = show
    }

    fun showTitle(): LiveData<Boolean> = showTitle

    fun pronounceWord(url: String) {
        try {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(url)
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
            )
            mediaPlayer.setOnPreparedListener {
                it.start()
            }
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            Log.d("AUDIO URL", url)
            Log.d("AUDIO ERROR", e.toString())
        }
    }

    fun navigateToMerriamWebster(): LiveData<Event<String>> = navigateToMerriamWebster

    fun bookmark() {
        GlobalScope.launch(Dispatchers.Main) {
            bookmarkRepo.bookmarkToggle(wordOfTheDay.word!!)
        }
    }

    class Factory(
        private val application: Application,
        private val word: WordOfTheDay
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return WordDetailedViewModel(application, word) as T
        }

    }

}