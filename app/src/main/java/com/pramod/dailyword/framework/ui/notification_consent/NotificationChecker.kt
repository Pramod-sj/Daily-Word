package com.pramod.dailyword.framework.ui.notification_consent

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


interface NotificationPermissionHandler {


    val isNotificationPermissionGranted: Flow<Boolean>

    fun launch(): Boolean

}

class ActivityNotificationPermissionHandler @Inject constructor(
    private val notificationChecker: NotificationChecker,
    @ActivityContext private val context: Context,
    private val prefManager: PrefManager
) : NotificationPermissionHandler, DefaultLifecycleObserver {

    private val activity = context as ComponentActivity

    private val _isNotificationPermissionGranted = Channel<Boolean>(1)
    override val isNotificationPermissionGranted: Flow<Boolean>
        get() = _isNotificationPermissionGranted.receiveAsFlow()

    private var isNavigatedToAppDetailPage = false

    private val notificationPermissionResult =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            if (permissionGranted) {
                _isNotificationPermissionGranted.trySend(true)
                notificationChecker.updateNotificationState()
            }
        }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        if (isNavigatedToAppDetailPage) {
            isNavigatedToAppDetailPage = false
            _isNotificationPermissionGranted.trySend(notificationChecker.isNotificationEnabled())
        }
        notificationChecker.updateNotificationState()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        activity.lifecycle.removeObserver(this)
        super.onDestroy(owner)
    }

    override fun launch(): Boolean {

        if (notificationChecker.isNotificationEnabled()) return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                prefManager.markNotificationPermissionAsked()
                notificationPermissionResult.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                if (!prefManager.isNotificationPermissionAsked()) {
                    notificationPermissionResult.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    val intent = Intent()
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", activity.packageName, null)
                    intent.setData(uri)
                    activity.startActivity(intent)
                    isNavigatedToAppDetailPage = true
                }
            }
        } else {
            val intent = Intent()
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.setData(uri)
            activity.startActivity(intent)
            isNavigatedToAppDetailPage = true
        }
        return true
    }

    init {
        activity.lifecycle.addObserver(this)
    }

}

@Singleton
class NotificationChecker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefManager: PrefManager,
) {

    private val notificationManager = NotificationManagerCompat.from(context)


    private val _isNotificationEnabled = MutableLiveData<Boolean>()
    val isNotificationEnabled: LiveData<Boolean>
        get() = _isNotificationEnabled


    private val _canShowNotificationEnableMessage = MutableLiveData(false)
    val canShowNotificationEnableMessage: LiveData<Boolean>
        get() = _canShowNotificationEnableMessage


    private val _canShowFullNotificationEnableMessage = MutableLiveData(false)
    val canShowFullNotificationEnableMessage: LiveData<Boolean>
        get() = _canShowFullNotificationEnableMessage

    fun isNotificationEnabled(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }

    fun updateNotificationState() {
        _isNotificationEnabled.value = isNotificationEnabled()
        _canShowNotificationEnableMessage.value =
            !isNotificationEnabled() && !prefManager.isSmallNotificationMessageDismissed()
        _canShowFullNotificationEnableMessage.value =
            !isNotificationEnabled() && !prefManager.isFullNotificationMessageDismissed()
    }

    fun markSmallNotificationRequestDismissed() {
        prefManager.markSmallNotificationMessageDismissed()
        updateNotificationState()
    }

    fun markFullNotificationRequestDismissed() {
        prefManager.markFullNotificationMessageDismissed()
        updateNotificationState()
    }

    init {
        updateNotificationState()
    }

}