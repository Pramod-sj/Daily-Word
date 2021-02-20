package com.pramod.dailyword.framework.ui.settings

import androidx.lifecycle.LiveData
import com.pramod.dailyword.framework.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppSettingViewModel @Inject constructor() : BaseViewModel() {

    var settingUseCase: SettingUseCase? = null

    var themeValue: LiveData<String>? = null

    var windowAnimValue: LiveData<Boolean>? = null

    var edgeToEdgeValue: LiveData<Boolean>? = null

    var dailyWordNotificationValue: LiveData<Boolean>? = null

    var reminderNotificationValue: LiveData<Boolean>? = null

}