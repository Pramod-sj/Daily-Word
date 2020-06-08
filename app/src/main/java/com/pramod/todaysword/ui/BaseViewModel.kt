package com.pramod.todaysword.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pramod.todaysword.Navigate
import com.pramod.todaysword.SnackbarMessage
import com.pramod.todaysword.util.Event

open class BaseViewModel(application: Application) : AndroidViewModel(application) {

    private val messageLiveData = MutableLiveData<Event<SnackbarMessage>>()

    fun setMessage(snackbarMessage: SnackbarMessage) {
        messageLiveData.value = Event.init(snackbarMessage)
    }

    fun getMessage(): LiveData<Event<SnackbarMessage>> = messageLiveData

}