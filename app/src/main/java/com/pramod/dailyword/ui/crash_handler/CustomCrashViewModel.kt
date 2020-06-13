package com.pramod.dailyword.ui.crash_handler

import android.app.Application
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.ui.BaseViewModel
import com.pramod.dailyword.util.Event

class CustomCrashViewModel(application: Application, errorStack: String) :
    BaseViewModel(application) {
    private val closeActivity = MutableLiveData<Unit>()
    private val clearAppData = MutableLiveData<Event<Boolean>>()
    private val sendMail = MutableLiveData<Event<String>>()
    private val restartApp = MutableLiveData<Event<Boolean>>()
    val errorStack = MutableLiveData<String>().apply {
        val errorDetails = StringBuilder(errorStack)
        errorDetails.append("\n App version:" + BuildConfig.VERSION_NAME)
        errorDetails.append("\n Model:" + Build.MODEL)
        errorDetails.append("\n Android SDK:" + Build.VERSION.SDK_INT)
        errorDetails.append("\n Brand :" + Build.BRAND)
        errorDetails.append("\n Manufacturer:" + Build.MANUFACTURER)
        value = errorDetails.toString()
    }

    fun restartAppLiveData(): LiveData<Event<Boolean>> {
        return restartApp
    }

    fun closeActivityLiveData(): LiveData<Unit> {
        return closeActivity
    }

    fun clearAppDataLiveData(): LiveData<Event<Boolean>> {
        return clearAppData
    }

    fun sendMailLiveData(): MutableLiveData<Event<String>> {
        return sendMail
    }

    fun restartApp() {
        restartApp.value = Event.init(true)
    }

    fun close() {
        closeActivity.value = null
    }

    fun sendMail() {
        sendMail.value = Event.init(errorStack.value!!)
    }

    fun clearAppData() {
        clearAppData.value = Event.init(true)
    }

    class Factory(val application: Application, val errorStack: String) :
        ViewModelProvider.AndroidViewModelFactory(application) {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CustomCrashViewModel(application, errorStack) as T
        }
    }
}