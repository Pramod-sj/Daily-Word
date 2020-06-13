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
import com.pramod.dailyword.ui.BaseViewModel

class WordDetailedViewModel(application: Application, val wordOfTheDay: WordOfTheDay) :
    BaseViewModel(application) {

    private val showTitle = MutableLiveData<Boolean>().apply {
        value = false
    }

    fun setTitleVisibility(show: Boolean) {
        showTitle.value = show
    }

    fun showTitle(): LiveData<Boolean> = showTitle

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


    class Factory(
        private val application: Application,
        private val wordOfTheDay: WordOfTheDay
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return WordDetailedViewModel(application, wordOfTheDay) as T
        }

    }

}