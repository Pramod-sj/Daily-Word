package com.pramod.dailyword.framework.prefmanagers

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WindowAnimPrefManager @Inject constructor(
    @ApplicationContext val context: Context
) : BasePreferenceManager(
    PREFERENCES_NAME, context
) {

    companion object {
        private const val PREFERENCES_NAME = "window_animation_preferences"
        private const val KEY_ANIMATION_ENABLED = "animation_enabled"

        private fun isAtLeastP() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

        fun newInstance(context: Context) = WindowAnimPrefManager(context)
    }

    fun toggle() {
        editor.putBoolean(
            KEY_ANIMATION_ENABLED,
            !isEnabled()
        ).commit()
    }

    fun isEnabled() = sPrefManager.getBoolean(
        KEY_ANIMATION_ENABLED,
        isAtLeastP()
    ) /*false*/


    fun liveData() = SPrefBooleanLiveData(sPrefManager, KEY_ANIMATION_ENABLED, isAtLeastP())


}