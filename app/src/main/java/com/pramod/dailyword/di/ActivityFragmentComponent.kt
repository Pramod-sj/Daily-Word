package com.pramod.dailyword.di

import android.content.Context
import com.pramod.dailyword.framework.prefmanagers.ThemeManager
import com.pramod.dailyword.framework.prefmanagers.EdgeToEdgePrefManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ActivityContext

@Module
@InstallIn(value = [ActivityComponent::class, FragmentComponent::class])
object ActivityFragmentComponent {

    @Provides
    fun provideThemeManager(
        @ActivityContext context: Context
    ): ThemeManager {
        return ThemeManager.newInstance(context)
    }

    @Provides
    fun provideWindowPrefManager(
        @ActivityContext context: Context
    ): EdgeToEdgePrefManager {
        return EdgeToEdgePrefManager.newInstance(context)
    }

}