package com.pramod.dailyword.framework.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import javax.inject.Inject

abstract class BaseViewModel : ViewModel() {

    @Inject
    lateinit var fbRemoteConfig: FBRemoteConfig

    //this variable is used for applying edge to edge for all the screens
    //this is added in base viewmodel because we need to use in all the screens
    var isEdgeToEdgeEnabled: Boolean? = null

    //this variable hold whether night mode is enabled
    var isNightMode: Boolean? = null

    private val _message = MutableLiveData<Message?>()


    val message: LiveData<Message?>
        get() = _message

    fun setMessage(message: Message?) {
        _message.value = message
    }
}
