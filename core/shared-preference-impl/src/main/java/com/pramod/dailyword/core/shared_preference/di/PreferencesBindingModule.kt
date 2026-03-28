package com.pramod.dailyword.core.shared_preference.di

import com.pramod.dailyword.core.preferences.RulesPreference
import com.pramod.dailyword.core.shared_preference.SharedPreferencesRulesPreference
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesBindingModule {

    // Binds the interface to our new SharedPreferences implementation
    @Binds
    internal abstract fun bindRulesPreference(
        impl: SharedPreferencesRulesPreference
    ): RulesPreference
}