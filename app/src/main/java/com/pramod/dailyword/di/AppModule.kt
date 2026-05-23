package com.pramod.dailyword.di

import android.app.AlarmManager
import android.app.job.JobScheduler
import android.appwidget.AppWidgetManager
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.judemanutd.autostarter.AutoStartPermissionHelper
import com.library.audioplayer.AudioPlayer
import com.pramod.dailyword.business.domain.util.ResourceProvider
import com.pramod.dailyword.framework.EndpointProviderImpl
import com.pramod.dailyword.framework.firebase.FBRemoteConfig
import com.pramod.dailyword.framework.haptics.AndroidHapticFeedbackManager
import com.pramod.dailyword.framework.haptics.HapticFeedbackManager
import com.pramod.dailyword.framework.prefmanagers.PrefManager
import com.pramod.dailyword.framework.util.ResourceProviderImpl
import com.pramod.dailyword.games.results.di.ResultsDataModule
import com.pramod.dailyword.games.results.di.ResultsModule
import com.pramod.dailyword.network.EndpointProvider
import com.pramod.dailyword.network.di.NetworkCoreInstance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module(includes = [ResultsDataModule::class, ResultsModule::class])
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

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideHapticFeedbackManager(
        @ApplicationContext context: Context,
        pref: PrefManager
    ): HapticFeedbackManager = AndroidHapticFeedbackManager(context, pref)


    @Provides
    @Singleton
    fun provideResourceProvider(@ApplicationContext context: Context): ResourceProvider {
        return ResourceProviderImpl(context)
    }

    @Provides
    @Singleton
    @NetworkCoreInstance
    fun provideGameEndpointProvider(
        remoteConfig: FBRemoteConfig
    ): EndpointProvider = EndpointProviderImpl(remoteConfig)


}