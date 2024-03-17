package com.pramod.dailyword.di

import com.pramod.dailyword.framework.ui.notification_consent.ActivityImportantPermissionHandler
import com.pramod.dailyword.framework.ui.notification_consent.ImportantPermissionHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped


@Module
@InstallIn(ActivityComponent::class)
interface NotificationModule {

    @ActivityScoped
    @Binds
    fun bindNotificationHandler(activityNotificationPermissionHandler: ActivityImportantPermissionHandler): ImportantPermissionHandler

}