package com.pramod.dailyword.framework.helper

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdateHelper @Inject constructor(@ApplicationContext val context: Context) {
    private val appUpdateManager = AppUpdateManagerFactory.create(context)
    private var onUpdateListener: AppUpdateListener? = null

    fun setUpdateListener(onUpdateListener: AppUpdateListener) {
        this.onUpdateListener = onUpdateListener
    }

    companion object {
        private const val IMMEDIATE_UPDATE_REQUEST_CODE = 1234
    }


    fun checkForUpdate(appUpdateAvailabilityListener: AppUpdateAvailabilityListener) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                appUpdateAvailabilityListener.onUpdateAvailable(it)
            } else {
                appUpdateAvailabilityListener.onUpdateNotAvailable()
            }
        }
    }

    fun startImmediateUpdate(
        appUpdateInfo: AppUpdateInfo,
        errorCallback: (String) -> Unit
    ) {
        if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
            try {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo, AppUpdateType.IMMEDIATE, context as Activity,
                    IMMEDIATE_UPDATE_REQUEST_CODE
                )
            } catch (e: Exception) {
                errorCallback.invoke(e.toString())
            }
        } else {
            errorCallback.invoke("Immediate update not possible")
        }
    }

    interface AppUpdateAvailabilityListener {
        fun onUpdateAvailable(appUpdateInfo: AppUpdateInfo)
        fun onUpdateNotAvailable()
    }


    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IMMEDIATE_UPDATE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                onUpdateListener?.onUpdated()
            } else {
                onUpdateListener?.onFailure("Failed to update!")
            }
        }
    }

    interface AppUpdateListener {
        fun onUpdated()
        fun onFailure(error: String)
    }

}