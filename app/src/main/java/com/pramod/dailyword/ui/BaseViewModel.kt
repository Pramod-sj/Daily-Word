package com.pramod.dailyword.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pramod.dailyword.SnackbarMessage
import com.pramod.dailyword.util.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

open class BaseViewModel(application: Application) : AndroidViewModel(application) {

    private val messageLiveData = MutableLiveData<Event<SnackbarMessage>>()

    fun setMessage(snackbarMessage: SnackbarMessage) {
        messageLiveData.value = Event.init(snackbarMessage)
    }

    fun getMessage(): LiveData<Event<SnackbarMessage>> = messageLiveData

    private var coroutineScope: CoroutineScope? = null

    fun getCoroutineScope(context: CoroutineContext = Dispatchers.IO): CoroutineScope {
        return coroutineScope ?: setupNewChannelScope(CoroutineScope(context))
    }

    private fun setupNewChannelScope(coroutineScope: CoroutineScope): CoroutineScope {
        this.coroutineScope = coroutineScope
        return coroutineScope
    }
}