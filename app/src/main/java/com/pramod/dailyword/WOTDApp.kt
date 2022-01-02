package com.pramod.dailyword

import android.app.Application
import android.content.Context
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import com.pramod.dailyword.framework.prefmanagers.ThemeManager
import com.pramod.dailyword.framework.util.CustomExceptionHandler
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber


@HiltAndroidApp
class WOTDApp : Application() {

    private val themeManager: ThemeManager by lazy {
        return@lazy ThemeManager.newInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        themeManager.applyTheme()
        //setUpCustomCrashHandler()
        initAds()
    }

    companion object {
        @JvmStatic
        fun clearAppData(context: Context): Boolean {
            return try {
                val packageName: String = context.packageName
                val runtime = Runtime.getRuntime()
                runtime.exec("pm clear $packageName")
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun initAds() {
        AdSettings.addTestDevice("4f19fa27-300a-4786-b9b0-30febb7ad630")
        AudienceNetworkAds.initialize(this)
    }

    private fun setUpCustomCrashHandler() {
        if (Thread.getDefaultUncaughtExceptionHandler() !is CustomExceptionHandler) {
            Thread.setDefaultUncaughtExceptionHandler(CustomExceptionHandler(this))
        }
    }


}