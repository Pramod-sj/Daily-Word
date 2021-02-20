package com.pramod.dailyword

import android.app.Application
import android.content.Context
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import com.pramod.dailyword.framework.prefmanagers.ThemeManager
import com.pramod.dailyword.framework.util.CustomExceptionHandler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WOTDApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ThemeManager.newInstance(this).applyTheme()
        //setUpCustomCrashHandler()
        initAds()
        PrefManager.getInstance(this).incrementAppLaunchCount()
    }

    companion object {
        @JvmStatic
        fun clearAppData(context: Context) {
            try {
                val packageName: String = context.packageName
                val runtime = Runtime.getRuntime()
                runtime.exec("pm clear $packageName")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initAds() {
        AdSettings.addTestDevice("4f19fa27-300a-4786-b9b0-30febb7ad630");
        AudienceNetworkAds.initialize(this)
    }

    private fun setUpCustomCrashHandler() {
        if (Thread.getDefaultUncaughtExceptionHandler() !is CustomExceptionHandler) {
            Thread.setDefaultUncaughtExceptionHandler(CustomExceptionHandler(this))
        }
    }


}