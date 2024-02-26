package com.pramod.dailyword.di

import com.pramod.dailyword.framework.ui.notification_consent.ActivityNotificationPermissionHandler
import com.pramod.dailyword.framework.ui.notification_consent.NotificationPermissionHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(ActivityComponent::class)
interface NotificationModule {

    @ActivityScoped
    @Binds
    fun bindNotificationHandler(activityNotificationPermissionHandler: ActivityNotificationPermissionHandler): NotificationPermissionHandler

}