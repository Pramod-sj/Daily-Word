package com.pramod.dailyword.di

import com.pramod.dailyword.framework.helper.NotificationHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationHelperEntryPoint {

    fun notificationHelper(): NotificationHelper

}