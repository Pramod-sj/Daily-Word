package com.pramod.dailyword.framework.ui.settings

import androidx.lifecycle.MutableLiveData
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.framework.prefmanagers.NotificationPrefManager
import com.pramod.dailyword.framework.ui.common.BaseViewModel
import com.pramod.dailyword.framework.widget.pref.WidgetPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppSettingViewModel @Inject constructor(
    val notificationPrefManager: NotificationPrefManager,
    val widgetPreference: WidgetPreference
) : BaseViewModel() {

    var settingUseCase: SettingUseCase? = null

    val themeValue = MutableLiveData<String>()

    val windowAnimValue = MutableLiveData<Boolean>()

    val edgeToEdgeValue = MutableLiveData<Boolean>()

    val hideBadgesValue = MutableLiveData<Boolean>()

    val subTitleCheckForUpdate = MutableLiveData<String>()

}