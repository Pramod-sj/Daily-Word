package com.pramod.dailyword.framework.ui.changelogs

import com.pramod.dailyword.framework.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChangelogViewModel @Inject constructor() : BaseViewModel() {
    var navigator: ChangelogNavigator? = null
}