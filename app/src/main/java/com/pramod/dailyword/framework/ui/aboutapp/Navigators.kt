package com.pramod.dailyword.framework.ui.aboutapp
interface AppLinkNavigate {
    fun navigateToForkProject()
    fun openGooglePlayReview()
    fun openDonatePage()
    fun shareAppWithFriends()
    fun openChangelogActivity()
}

interface DeveloperLinkNavigate {
    fun navigateToGithub()
    fun navigateToFacebook()
    fun navigateToGmail()
    fun navigateToInstagram()
}

interface OtherLinkNavigate {
    fun showTermsAndService()
    fun showPrivacyPolicy()
    fun showOpenSourceLibs()
}

interface CreditLinkNavigate {
    fun navigateToFreePikWebsite()
    fun navigateToMaterialDesignIcon()
    fun navigateToMerriamWebster()
}