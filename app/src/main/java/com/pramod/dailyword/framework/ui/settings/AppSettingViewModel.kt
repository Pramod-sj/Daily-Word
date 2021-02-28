package com.pramod.dailyword.framework.ui.settings

import androidx.lifecycle.MutableLiveData
import com.pramod.dailyword.framework.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppSettingViewModel @Inject constructor() : BaseViewModel() {

    var settingUseCase: SettingUseCase? = null

    val themeValue = MutableLiveData<String>()

    val windowAnimValue = MutableLiveData<Boolean>()

    val edgeToEdgeValue = MutableLiveData<Boolean>()

    val dailyWordNotificationValue = MutableLiveData<Boolean>()

    val reminderNotificationValue = MutableLiveData<Boolean>()

}