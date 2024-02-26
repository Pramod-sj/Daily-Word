package com.pramod.dailyword.framework.ui.home

import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pramod.dailyword.framework.prefmanagers.DisableBatteryOptimizationPermissionPref
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

class BatteryOptimizationPermissionHandler @Inject constructor(
    @ActivityContext private val context: Context,
    private val batteryOptimizationChecker: BatteryOptimizationChecker
) : DefaultLifecycleObserver {

    private val activity = context as ComponentActivity

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        batteryOptimizationChecker.updateBatteryOptimizationDisabledState()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        activity.lifecycle.removeObserver(this)
        super.onDestroy(owner)
    }

    fun launch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            activity.startActivity(intent)
        }
    }

    init {
        activity.lifecycle.addObserver(this)
    }

}

@Singleton
class BatteryOptimizationChecker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefManager: DisableBatteryOptimizationPermissionPref
) {

    private val powerManager = context.getSystemService(POWER_SERVICE) as PowerManager

    private val _isBatteryOptimizationDisabled = MutableLiveData(false)
    val isBatteryOptimizationDisabled: LiveData<Boolean>
        get() = _isBatteryOptimizationDisabled

    private val _canShowDisableBatteryOptimizationMessage = MutableLiveData(false)
    val canShowDisableBatteryOptimizationMessage: LiveData<Boolean>
        get() = _canShowDisableBatteryOptimizationMessage

    fun updateBatteryOptimizationDisabledState() {
        _isBatteryOptimizationDisabled.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
        _canShowDisableBatteryOptimizationMessage.value =
            _isBatteryOptimizationDisabled.value == false
                    && !prefManager.isDisableBatteryOptimizationDismissed()
    }

    fun markNotInterestedForBatteryOptimization() {
        prefManager.markDisableBatteryOptimizationDismissed()
        updateBatteryOptimizationDisabledState()
    }

    init {
        updateBatteryOptimizationDisabledState()
    }

}