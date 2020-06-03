package com.pramod.todaysword.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.pramod.todaysword.Navigate
import com.pramod.todaysword.SnackbarMessage
import com.pramod.todaysword.util.Event

open class BaseViewModel(application: Application) : AndroidViewModel(application) {
    val loadingLiveData = MutableLiveData<Event<Boolean>>()
    val messageLiveData = MutableLiveData<Event<SnackbarMessage>>()

    fun showLoading(show: Boolean) {
        loadingLiveData.value = Event.init(show)
    }


    fun setMessage(snackbarMessage: SnackbarMessage) {
        messageLiveData.value = Event.init(snackbarMessage)
    }

}