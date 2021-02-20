package com.pramod.dailyword.framework.ui.settings

interface SettingUseCase {
    fun openChooseThemeDialog()
    fun toggleWindowAnimation()
    fun toggleEdgeToEdge()
    fun toggleDailyWordNotification()
    fun toggleReminderNotification()
    fun navigateToFacingNotificationIssue()
    fun navigateToAbout()
    fun copyFirebaseTokenId()
}