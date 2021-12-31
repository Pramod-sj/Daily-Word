package com.pramod.dailyword.di

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

}