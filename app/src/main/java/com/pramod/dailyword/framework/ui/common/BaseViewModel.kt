package com.pramod.dailyword.framework.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    //this variable is used for applying edge to edge for all the screens
    //this is added in base viewmodel because we need to use in all the screens
    var isEdgeToEdgeEnabled: Boolean? = null

    private val _message = MutableLiveData<Message?>()
    val message: LiveData<Message?>
        get() = _message

    private val _event = Channel<Event>(Channel.Factory.BUFFERED)
    val event: Flow<Event> = _event.receiveAsFlow()

    fun setMessage(message: Message?) {
        _message.value = message
    }

    fun setEvent(event: Event) {
        viewModelScope.launch {
            _event.send(event)
        }
    }

}


sealed interface Event {

    /**
     * Navigation effect - emitted when a router action needs UI layer handling.
     * Use this for cases where the click handler can't directly navigate
     * (e.g., needs Fragment/Activity context for specific navigations).
     */
    data class Navigate(val action: CommonNavigationAction) : Event

}


sealed interface CommonNavigationAction {

    data object ShowInterstitialAd : CommonNavigationAction

    data object ShowRewardedAd : CommonNavigationAction

    data object ShowDoNotShowRewardAdsDialog : CommonNavigationAction

}