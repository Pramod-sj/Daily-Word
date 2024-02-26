package com.pramod.dailyword.di

import com.pramod.dailyword.framework.prefmanagers.DisableBatteryOptimizationPermissionPref
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import com.pramod.dailyword.framework.prefmanagers.WidgetSettingPreference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(value = [SingletonComponent::class])
object PreferenceModule {

    @Provides
    fun provideWidgetSettingPreference(prefManager: PrefManager): WidgetSettingPreference {
        return prefManager
    }

    @Provides
    fun provideDisableBatteryOptimizationPref(prefManager: PrefManager): DisableBatteryOptimizationPermissionPref {
        return prefManager
    }

}