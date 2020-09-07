package com.pramod.dailyword.ui.home

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.util.Log
import androidx.lifecycle.*
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.SnackbarMessage
import com.pramod.dailyword.db.Resource
import com.pramod.dailyword.db.repository.WOTDRepository
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.helper.PrefManager
import com.pramod.dailyword.ui.BaseViewModel
import com.pramod.dailyword.util.CommonUtils
import com.pramod.dailyword.util.Event
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : BaseViewModel(application) {
    private val wordOfTheDayRepo: WOTDRepository =
        WOTDRepository(application)

    private val title = MutableLiveData<String>().apply {
        value = CommonUtils.getGreetMessage()
    }

    fun title(): LiveData<String> = title

    private val wordOfTheDayLoading: MutableLiveData<Event<Boolean>> = MutableLiveData()

    private val learnAll: MutableLiveData<Event<Boolean>> = MutableLiveData()

    private lateinit var wordOfTheDayLiveData: LiveData<WordOfTheDay?>
    private lateinit var wordsExceptTodayLiveData: LiveData<List<WordOfTheDay>?>


    private val navigateToWordDetailedActivity: MutableLiveData<Event<SelectedItem<WordOfTheDay>>> =
        MutableLiveData()

    private val navigateToWordDetailedActivityFromTodaysCard: MutableLiveData<Event<WordOfTheDay>> =
        MutableLiveData()


    init {
        wordOfTheDayLoading.value = Event.init(true)
        val wordResourceLiveData = wordOfTheDayRepo.getWords()
        wordOfTheDayLiveData = Transformations.map(wordResourceLiveData) {
            wordOfTheDayLoading.value = Event.init(it.status == Resource.Status.LOADING)
            if (it.status != Resource.Status.LOADING) {
                Handler().postDelayed({
                    title.postValue(
                        "Here's your Daily Word"
                    )
                }, 2000)
            }
            if (it.status == Resource.Status.ERROR) {
                it.message?.let { message ->
                    setMessage(SnackbarMessage.init(message))
                }
            }
            return@map if (it.data != null && it.data.isNotEmpty()) it.data[0] else null
        }

        wordsExceptTodayLiveData = Transformations.map(wordResourceLiveData) {
            val list = it.data?.toMutableList()
            if (list != null && list.isNotEmpty()) {
                list.removeAt(0)
            }
            return@map list
        }

        /*wordOfTheDayRepo.initGetWordOfTheDayWorker()
        wordOfTheDayWorkerLiveData = wordOfTheDayRepo.getDailyWordOfTheDayWorker()

        wordOfTheDayRepo.initRemindWordOfTheDay()
        newWordReminderWorkerLiveData = wordOfTheDayRepo.getRemindWordOfTheDay()*/
    }


    fun getTodaysWordOfTheDay(): LiveData<WordOfTheDay?> {
        return wordOfTheDayLiveData
    }


    fun getWordsExceptToday(): LiveData<List<WordOfTheDay>?> {
        return wordsExceptTodayLiveData
    }

    fun pronounceWord(url: String) {
        Log.d("AUDIO URL", url)
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
            Log.d("AUDIO ERROR", e.toString())
        }
    }


    fun navigateToWordDetailed(selectedItem: SelectedItem<WordOfTheDay>) {
        navigateToWordDetailedActivity.value = Event.init(selectedItem)
    }


    fun navigateToWordDetailedFromTodaysWord(word: WordOfTheDay) {
        navigateToWordDetailedActivityFromTodaysCard.value = Event.init(word)
    }

    fun observeNavigateToWordDetailedEvent(): LiveData<Event<SelectedItem<WordOfTheDay>>> =
        navigateToWordDetailedActivity

    fun observeNavigateToWordDetailedEventFromTodaysCard(): LiveData<Event<WordOfTheDay>> =
        navigateToWordDetailedActivityFromTodaysCard


    fun updateWordSeenStatus(word: WordOfTheDay) {
        viewModelScope.launch {
            wordOfTheDayRepo.updateWord(word)
        }
    }

    fun learnAll() {
        learnAll.value = Event.init(true)
    }

    fun getLearnAllLiveData(): LiveData<Event<Boolean>> {
        return learnAll
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
}