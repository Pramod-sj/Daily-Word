package com.pramod.dailyword.helper

import android.R
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.Log
import android.view.View
import android.view.Window
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.graphics.ColorUtils
import com.google.android.material.color.MaterialColors

class WindowPrefManager private constructor(private val context: Context) {


    private val sharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()


    companion object {
        private const val PREFERENCES_NAME = "window_preferences"
        private const val KEY_EDGE_TO_EDGE_ENABLED = "edge_to_edge_enabled"
        private const val EDGE_TO_EDGE_BAR_ALPHA = 128

        @RequiresApi(VERSION_CODES.LOLLIPOP)
        private val EDGE_TO_EDGE_FLAGS =
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        private fun isColorLight(@ColorInt color: Int): Boolean {
            return color != Color.TRANSPARENT && ColorUtils.calculateLuminance(
                color
            ) > 0.5
        }

        fun newInstance(context: Context) =
            WindowPrefManager(
                context
            )
    }


    fun toggleEdgeToEdgeEnabled() {
        Log.i("EDGE TO EDGE TOGGLE", (!isEdgeToEdgeEnabled()).toString())
        editor.putBoolean(
            KEY_EDGE_TO_EDGE_ENABLED,
            !isEdgeToEdgeEnabled()
        ).commit()
    }

    fun isEdgeToEdgeEnabled() = sharedPreferences.getBoolean(
        KEY_EDGE_TO_EDGE_ENABLED,
        false
    )

    fun applyEdgeToEdgeIfEnabled(window: Window, forceApply: Boolean = false) {
        val edgeToEdgeEnabled = if (forceApply) true else isEdgeToEdgeEnabled()
        applyEdgeToEdgePreference(window, edgeToEdgeEnabled)
    }

    fun applyEdgeToEdgePreference(window: Window, shouldApply: Boolean) {
        val statusBarColor = getStatusBarColor(isEdgeToEdgeEnabled())
        val navbarColor = getNavBarColor(isEdgeToEdgeEnabled())
        val lightBackground =
            isColorLight(
                MaterialColors.getColor(
                    context,
                    R.attr.colorBackground,
                    Color.BLACK
                )
            )
        val lightNavbar =
            isColorLight(
                navbarColor
            )
        val showDarkNavbarIcons =
            lightNavbar || navbarColor == Color.TRANSPARENT && lightBackground
        val decorView = window.decorView
        val currentStatusBar =
            if (VERSION.SDK_INT >= VERSION_CODES.M) decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else 0
        val currentNavBar =
            if (showDarkNavbarIcons && VERSION.SDK_INT >= VERSION_CODES.O) View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR else 0
        window.navigationBarColor = navbarColor
        window.statusBarColor = statusBarColor
        val systemUiVisibility =
            ((if (shouldApply) EDGE_TO_EDGE_FLAGS else View.SYSTEM_UI_FLAG_VISIBLE)
                    or currentStatusBar
                    or currentNavBar)
        decorView.systemUiVisibility = systemUiVisibility
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    private fun getStatusBarColor(isEdgeToEdgeEnabled: Boolean): Int {
        if (isEdgeToEdgeEnabled && VERSION.SDK_INT < VERSION_CODES.M) {
            val opaqueStatusBarColor = MaterialColors.getColor(
                context,
                R.attr.statusBarColor,
                Color.BLACK
            )
            return ColorUtils.setAlphaComponent(
                opaqueStatusBarColor,
                EDGE_TO_EDGE_BAR_ALPHA
            )
        }
        return if (isEdgeToEdgeEnabled) {
            Color.TRANSPARENT
        } else MaterialColors.getColor(
            context,
            R.attr.statusBarColor,
            Color.BLACK
        )
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    private fun getNavBarColor(isEdgeToEdgeEnabled: Boolean): Int {
        if (isEdgeToEdgeEnabled && VERSION.SDK_INT < VERSION_CODES.O_MR1) {
            val opaqueNavBarColor = MaterialColors.getColor(
                context,
                R.attr.navigationBarColor,
                Color.BLACK
            )
            return ColorUtils.setAlphaComponent(
                opaqueNavBarColor,
                EDGE_TO_EDGE_BAR_ALPHA
            )
        }
        return if (isEdgeToEdgeEnabled) {
            Color.TRANSPARENT
        } else MaterialColors.getColor(
            context,
            R.attr.navigationBarColor,
            Color.BLACK
        )
    }

    fun getLiveData(): SPrefBooleanLiveData {
        return SPrefBooleanLiveData(
            sharedPreferences,
            KEY_EDGE_TO_EDGE_ENABLED,
            false
        )
    }

}