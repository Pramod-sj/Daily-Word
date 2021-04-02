package com.pramod.dailyword.framework.ui.aboutapp

import com.pramod.dailyword.framework.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AboutAppViewModel @Inject constructor() : BaseViewModel() {
    var appLinkNavigate: AppLinkNavigate? = null
    var developerLinkNavigate: DeveloperLinkNavigate? = null
    var otherLinkNavigate: OtherLinkNavigate? = null
    var creditLinkNavigate: CreditLinkNavigate? = null
}
