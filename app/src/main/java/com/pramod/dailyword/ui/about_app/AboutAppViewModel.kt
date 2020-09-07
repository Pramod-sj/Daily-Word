package com.pramod.dailyword.ui.about_app

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pramod.dailyword.ui.BaseViewModel
import com.pramod.dailyword.util.Event

class AboutAppViewModel(application: Application) : BaseViewModel(application)

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