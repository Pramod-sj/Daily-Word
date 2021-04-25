package com.pramod.dailyword.framework.ui.settings

interface SettingUseCase {
    fun openChooseThemeDialog()
    fun toggleWindowAnimation()
    fun toggleEdgeToEdge()
    fun toggleDailyWordNotification()
    fun toggleReminderNotification()
    fun toggleBadgeVisibility()
    fun navigateToFacingNotificationIssue()
    fun checkForUpdate()
    fun navigateToAbout()
    fun copyFirebaseTokenId()
    fun clearAppData()
}