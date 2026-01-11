package com.pramod.dailyword.framework.ui.settings

interface SettingUseCase {
    fun openChooseThemeDialog()
    fun toggleWindowAnimation()
    fun toggleEdgeToEdge()
    fun toggleBadgeVisibility()
    fun checkForUpdate()
    fun navigateToAbout()
    fun copyFirebaseTokenId()
    fun clearAppData()
    fun showWidgetBackgroundDialog()
    fun showWidgetControlsDialog()
    fun toggleHaptic()
}