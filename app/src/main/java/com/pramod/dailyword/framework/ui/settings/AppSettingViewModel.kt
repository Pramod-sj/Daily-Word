package com.pramod.dailyword.framework.ui.settings

import androidx.lifecycle.MutableLiveData
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.framework.prefmanagers.NotificationPrefManager
import com.pramod.dailyword.framework.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppSettingViewModel @Inject constructor(
    val notificationPrefManager: NotificationPrefManager
) : BaseViewModel() {

    var settingUseCase: SettingUseCase? = null

    val themeValue = MutableLiveData<String>()

    val windowAnimValue = MutableLiveData<Boolean>()

    val edgeToEdgeValue = MutableLiveData<Boolean>()

    val hideBadgesValue = MutableLiveData<Boolean>()

    val subTitleCheckForUpdate = MutableLiveData<String>().apply {
        value = DEFAULT_MESSAGE_CHECK_FOR_UPDATE
    }

    companion object {
        const val DEFAULT_MESSAGE_CHECK_FOR_UPDATE =
            "You are currently on ${BuildConfig.VERSION_NAME}, Tap to check for update!"

        const val DEFAULT_MESSAGE_NEW_UPDATE_AVAILABLE_TO_DOWNLOAD =
            "A new version is available to download, Tap to download the update!"

        const val DEFAULT_MESSAGE_NEW_UPDATE_DOWNLOADING =
            "A new version is downloading, please wait..."

        const val DEFAULT_MESSAGE_NEW_UPDATE_AVAILABLE_TO_INSTALL =
            "A new version is available to install, Tap to proceed with installation process"


    }

}