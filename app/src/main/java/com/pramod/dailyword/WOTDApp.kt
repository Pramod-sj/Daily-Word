package com.pramod.dailyword

import android.app.Application
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
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
        //setUpCusomCrashHandler()
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
        MobileAds.initialize(this)
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("1A25639373E9445D562CBC2796BCAB5D")).build()
        )
    }

    private fun setUpCustomCrashHandler() {
        if (Thread.getDefaultUncaughtExceptionHandler() !is CustomExceptionHandler) {
            Thread.setDefaultUncaughtExceptionHandler(CustomExceptionHandler(this))
        }
    }
}