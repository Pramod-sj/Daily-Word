package com.pramod.dailyword.helper

import android.content.Context
import android.os.Build

class WindowAnimationPrefManager private constructor(private val context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()


    companion object {
        private const val PREFERENCES_NAME = "window_animation_preferences"
        private const val KEY_ANIMATION_ENABLED = "animation_enabled"

        private fun isAtLeastP() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

        fun newInstance(context: Context) = WindowAnimationPrefManager(context)
    }

    fun toggleWindowAnimationEnabled() {
        editor.putBoolean(
            KEY_ANIMATION_ENABLED,
            !isWindowAnimationEnabled()
        ).commit()
    }

    fun isWindowAnimationEnabled() = sharedPreferences.getBoolean(
        KEY_ANIMATION_ENABLED,
        isAtLeastP()
    ) /*false*/


    fun liveData() = SPrefBooleanLiveData(sharedPreferences, KEY_ANIMATION_ENABLED, isAtLeastP())


}