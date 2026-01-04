package com.pramod.dailyword.di

import android.app.Activity
import com.pramod.dailyword.framework.helper.ads.AdProvider
import com.pramod.dailyword.framework.helper.ads.GoogleAdProviderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent


@Module
@InstallIn(value = [ActivityComponent::class])
class AdModule {

    @Provides
    fun provideGoogleAdProvider(
        activity: Activity
    ): AdProvider = GoogleAdProviderImpl(activity)

}