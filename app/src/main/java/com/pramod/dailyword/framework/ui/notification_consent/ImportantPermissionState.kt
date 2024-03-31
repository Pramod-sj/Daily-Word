package com.pramod.dailyword.framework.ui.notification_consent

import android.Manifest
import android.app.AlarmManager
import android.app.AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pramod.dailyword.framework.helper.scheduleWeeklyAlarmAt12PM
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


interface ImportantPermissionHandler {

    val isNotificationPermissionGranted: Flow<Boolean>

    val isDisableBatteryOptimizationGranted: Flow<Boolean>

    val isAllowSettingAlarmPermissionGranted: Flow<Boolean>

    fun launchNotificationPermissionFlow(): Boolean

    fun launchDisableBatteryOptimizationPermissionFlow(): Boolean

    fun launchAllowSettingAlarmPermissionFlow(): Boolean

}

@ActivityScoped
class ActivityImportantPermissionHandler @Inject constructor(
    private val importantPermissionState: ImportantPermissionState,
    @ActivityContext private val context: Context,
    private val prefManager: PrefManager
) : ImportantPermissionHandler, DefaultLifecycleObserver {

    private val activity = context as ComponentActivity

    private val _isNotificationPermissionGranted = Channel<Boolean>(1)

    private val _isDisableBatteryOptimizationGranted = Channel<Boolean>(1)

    private val _isAllowSettingAlarmGranted = Channel<Boolean>(1)

    override val isNotificationPermissionGranted: Flow<Boolean>
        get() = _isNotificationPermissionGranted.receiveAsFlow()
    override val isDisableBatteryOptimizationGranted: Flow<Boolean>
        get() = _isDisableBatteryOptimizationGranted.receiveAsFlow()
    override val isAllowSettingAlarmPermissionGranted: Flow<Boolean>
        get() = _isAllowSettingAlarmGranted.receiveAsFlow()


    private var isNavigatedToAppDetailPage = false


    private val notificationPermissionResult =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            if (permissionGranted) {
                _isNotificationPermissionGranted.trySend(true)
                importantPermissionState.updatePermissionEnabledState()
            }
        }

    private val alarmReScheduleBroadcastListener by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED) {
                    Timber.i("Scheduling alarm at 12PM on Sunday")
                    context?.scheduleWeeklyAlarmAt12PM()
                }
            }
        }
    }

    override fun launchNotificationPermissionFlow(): Boolean {

        if (importantPermissionState.isNotificationEnabled.value) return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                prefManager.markNotificationPermissionAsked()
                notificationPermissionResult.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                if (!prefManager.isNotificationPermissionAsked()) {
                    notificationPermissionResult.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    val intent = Intent()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    } else {
                        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        intent.putExtra("app_package", context.packageName)
                        intent.putExtra("app_uid", context.applicationInfo.uid)
                    }
                    context.startActivity(intent);
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

    override fun launchDisableBatteryOptimizationPermissionFlow(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            activity.startActivity(intent)
            true
        } else false
    }

    override fun launchAllowSettingAlarmPermissionFlow(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            true
        } else false
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            activity.registerReceiver(
                alarmReScheduleBroadcastListener,
                IntentFilter(ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED)
            )
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        if (isNavigatedToAppDetailPage) {
            isNavigatedToAppDetailPage = false
            _isNotificationPermissionGranted.trySend(importantPermissionState.isNotificationEnabled.value)
        }
        importantPermissionState.updatePermissionEnabledState()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        activity.lifecycle.removeObserver(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            activity.unregisterReceiver(alarmReScheduleBroadcastListener)
        }
        super.onDestroy(owner)
    }

    init {
        activity.lifecycle.addObserver(this)
    }

}

@Singleton
class ImportantPermissionState @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefManager: PrefManager,
) {

    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(context)

    private val powerManager: PowerManager = context.getSystemService(POWER_SERVICE) as PowerManager

    private val alarmManager: AlarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager


    private val _isNotificationEnabled = MutableStateFlow(false)
    val isNotificationEnabled: StateFlow<Boolean>
        get() = _isNotificationEnabled


    private val _isBatteryOptimizationDisabled = MutableStateFlow(false)
    val isBatteryOptimizationDisabled: StateFlow<Boolean>
        get() = _isBatteryOptimizationDisabled


    private val _isSetAlarmEnabled = MutableStateFlow(false)
    val isSetAlarmEnabled: StateFlow<Boolean>
        get() = _isSetAlarmEnabled


    private val _canShowFullNotificationEnableMessage = MutableLiveData(false)
    val canShowFullNotificationEnableMessage: LiveData<Boolean>
        get() = _canShowFullNotificationEnableMessage

    fun updatePermissionEnabledState() {
        _isNotificationEnabled.value = notificationManager.areNotificationsEnabled()
        _isBatteryOptimizationDisabled.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
        _isSetAlarmEnabled.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else true

        _canShowFullNotificationEnableMessage.value =
            !isNotificationEnabled.value && !prefManager.isFullNotificationMessageDismissed()
    }

    fun markSettingIssueMessageDismissed() {
        prefManager.markSettingIssueWarningDismissed()
        updatePermissionEnabledState()
    }

    fun markFullNotificationRequestDismissed() {
        prefManager.markFullNotificationMessageDismissed()
        updatePermissionEnabledState()
    }

    init {
        updatePermissionEnabledState()
    }

}