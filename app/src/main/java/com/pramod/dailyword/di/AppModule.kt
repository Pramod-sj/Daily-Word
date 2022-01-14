package com.pramod.dailyword.di

import android.app.AlarmManager
import android.app.job.JobScheduler
import android.appwidget.AppWidgetManager
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.judemanutd.autostarter.AutoStartPermissionHelper
import com.library.audioplayer.AudioPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(value = [SingletonComponent::class])
object AppModule {

    @Provides
    @Singleton
    fun provideAudioPlayer(
        @ApplicationContext context: Context
    ): AudioPlayer {
        return AudioPlayer(context)
    }

    @Provides
    @Singleton
    fun provideAutoStartPermissionHelper(): AutoStartPermissionHelper {
        return AutoStartPermissionHelper.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideAppWidgetManager(@ApplicationContext context: Context): AppWidgetManager {
        return AppWidgetManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideJobScheduler(@ApplicationContext context: Context): JobScheduler {
        return context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    }

    @Provides
    @Singleton
    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager {
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

}