package com.pramod.dailyword.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pramod.dailyword.SnackbarMessage
import com.pramod.dailyword.util.Event

open class BaseViewModel(application: Application) : AndroidViewModel(application) {

    private val messageLiveData = MutableLiveData<Event<SnackbarMessage>>()

    fun setMessage(snackbarMessage: SnackbarMessage) {
        messageLiveData.value = Event.init(snackbarMessage)
    }

    fun getMessage(): LiveData<Event<SnackbarMessage>> = messageLiveData

}