package com.pramod.dailyword.framework.ui.settings

interface SettingUseCase {
    fun openChooseThemeDialog()
    fun toggleWindowAnimation()
    fun toggleEdgeToEdge()
    fun toggleBadgeVisibility()
    fun navigateToFacingNotificationIssue()
    fun navigateToAbout()
    fun copyFirebaseTokenId()
    fun clearAppData()
}