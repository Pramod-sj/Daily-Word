package com.pramod.dailyword.framework.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task

class AppUpdateHelperNew(
        private val context: Context,
        lifecycle: Lifecycle) : LifecycleObserver {

    private val appUpdateManager = AppUpdateManagerFactory.create(context)

    private var appUpdateInfo: AppUpdateInfo? = null

    init {
        lifecycle.addObserver(this)
    }


    fun checkForUpdate(checkingUpdateListener: CheckingUpdateListener?) {
        appUpdateManager.appUpdateInfo.addOnCompleteListener { task: Task<AppUpdateInfo?> ->
            if (task.isSuccessful) {
                if (checkingUpdateListener != null) {
                    appUpdateInfo = task.result
                    if (appUpdateInfo!!.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                            || appUpdateInfo!!.installStatus() == InstallStatus.DOWNLOADED) {
                        checkingUpdateListener.onUpdateAvailable(appUpdateInfo!!.availableVersionCode().toLong(), appUpdateInfo!!.installStatus() == InstallStatus.DOWNLOADED)
                    } else {
                        checkingUpdateListener.onUpdateNotAvailable()
                    }
                    Log.i(TAG, "checkForUpdate: " + appUpdateInfo.toString())
                }
            } else {
                checkingUpdateListener?.onFailed(task.exception!!.message)
            }
        }
    }

    fun showFlexibleDialog() {
        try {
            appUpdateManager.registerListener(installStateUpdatedListener)
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo!!,
                    AppUpdateType.FLEXIBLE,
                    (context as Activity),
                    APP_UPDATE_FLEX_REQUEST_CODE
            )
        } catch (e: IntentSender.SendIntentException) {
            e.printStackTrace()
        }
    }

    fun showImmediateDialog() {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo!!,
                    AppUpdateType.IMMEDIATE,
                    (context as Activity),
                    APP_UPDATE_IMMEDIATE_REQUEST_CODE
            )
        } catch (e: IntentSender.SendIntentException) {
            e.printStackTrace()
        }
    }

    fun startInstallationProcess() {
        appUpdateManager.completeUpdate().addOnCompleteListener { }
    }

    private var installStatusListener: InstallStatusListener? = null
    fun setInstallStatusListener(installStatusListener: InstallStatusListener?) {
        this.installStatusListener = installStatusListener
    }

    private val installStateUpdatedListener = InstallStateUpdatedListener { state: InstallState ->
        if (installStatusListener != null) {
            when (state.installStatus()) {
                InstallStatus.DOWNLOADING -> installStatusListener?.onDownloading()
                InstallStatus.DOWNLOADED -> installStatusListener?.onDownloaded()
                InstallStatus.INSTALLING -> installStatusListener?.onInstalling()
                InstallStatus.INSTALLED -> installStatusListener?.onInstalled()
                InstallStatus.FAILED, InstallStatus.UNKNOWN, InstallStatus.CANCELED -> installStatusListener?.onFailed()
                InstallStatus.PENDING -> installStatusListener?.onPending()
                InstallStatus.REQUIRES_UI_INTENT -> {
                    //not required to handle this status
                }
            }
        }
    }
    private var onAppUpdateActivityResultListener: OnAppUpdateActivityResultListener? = null
    fun setOnAppUpdateActivityResultListener(onAppUpdateActivityResultListener: OnAppUpdateActivityResultListener?) {
        this.onAppUpdateActivityResultListener = onAppUpdateActivityResultListener
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == APP_UPDATE_FLEX_REQUEST_CODE) {
            if (onAppUpdateActivityResultListener != null) {
                when (resultCode) {
                    Activity.RESULT_OK ->                         //  handle user's approval
                        onAppUpdateActivityResultListener!!.onUserApproval()
                    Activity.RESULT_CANCELED -> onAppUpdateActivityResultListener!!.onUserCancelled()
                    ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> onAppUpdateActivityResultListener!!.onUpdateFailure()
                }
            }
        }
    }

    interface CheckingUpdateListener {
        fun onUpdateAvailable(latestVersionCode: Long, isUpdateDownloaded: Boolean)
        fun onUpdateNotAvailable()
        fun onFailed(message: String?)
    }

    interface OnAppUpdateActivityResultListener {
        fun onUserApproval()
        fun onUserCancelled()
        fun onUpdateFailure()
    }

    abstract class InstallStatusListener {
        open fun onDownloading() {}
        open fun onDownloaded() {}
        open fun onInstalling() {}
        open fun onInstalled() {}
        open fun onFailed() {}
        open fun onPending() {}
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun dispose() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    companion object {
        const val APP_UPDATE_FLEX_REQUEST_CODE = 1
        const val APP_UPDATE_IMMEDIATE_REQUEST_CODE = 2
        private val TAG = AppUpdateHelper::class.java.simpleName
    }
}