package com.pramod.dailyword.framework.prefmanagers

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
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class EdgeToEdgePrefManager @Inject constructor(
    @ActivityContext private val context: Context
) :
    BasePreferenceManager(PREFERENCES_NAME, context) {

    companion object {
        val TAG = EdgeToEdgePrefManager::class.java.simpleName

        private const val VALUE_DEFAULT_EDGE_TO_EDGE = true

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
            EdgeToEdgePrefManager(
                context
            )
    }

    fun toggle() {
        Log.i("EDGE TO EDGE TOGGLE", (!isEnabled()).toString())
        editor.putBoolean(
            KEY_EDGE_TO_EDGE_ENABLED,
            !isEnabled()
        ).commit()
    }

    /**
     * edge to edge is turned on by default
     */
    fun isEnabled() = sPrefManager.getBoolean(
        KEY_EDGE_TO_EDGE_ENABLED,
        VALUE_DEFAULT_EDGE_TO_EDGE
    )

    fun applyEdgeToEdgeIfEnabled(
        window: Window,
        forceApply: Boolean? = false,
        transparentNavBar: Boolean? = false
    ) {
        val edgeToEdgeEnabled = if (forceApply == true) true else isEnabled()
        Log.i(TAG, "applyEdgeToEdgeIfEnabled: $edgeToEdgeEnabled")
        applyEdgeToEdgePreference(window, edgeToEdgeEnabled, transparentNavBar)
    }

    private fun applyEdgeToEdgePreference(
        window: Window,
        shouldApply: Boolean,
        transparentNavBar: Boolean? = false
    ) {
        val statusBarColor = getStatusBarColor(isEnabled())
        val navbarColor = getNavBarColor(isEnabled(), transparentNavBar)
        val lightBackground =
            isColorLight(
                MaterialColors.getColor(
                    context,
                    android.R.attr.colorBackground,
                    Color.BLACK
                )
            )
        val lightNavbar = isColorLight(navbarColor)
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
                android.R.attr.statusBarColor,
                Color.BLACK
            )
            return ColorUtils.setAlphaComponent(
                opaqueStatusBarColor,
                if (ThemeManager.isNightModeActive(context)) {
                    0
                } else EDGE_TO_EDGE_BAR_ALPHA
            )
        }
        return if (isEdgeToEdgeEnabled) {
            Color.TRANSPARENT
        } else MaterialColors.getColor(
            context,
            android.R.attr.statusBarColor,
            Color.BLACK
        )
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    private fun getNavBarColor(isEdgeToEdgeEnabled: Boolean, transparentNavBar: Boolean?): Int {
        if (isEdgeToEdgeEnabled && VERSION.SDK_INT < VERSION_CODES.O_MR1) {
            val opaqueNavBarColor = MaterialColors.getColor(
                context,
                android.R.attr.navigationBarColor,
                Color.BLACK
            )
            return ColorUtils.setAlphaComponent(
                opaqueNavBarColor,
                if (transparentNavBar == true) 0 else EDGE_TO_EDGE_BAR_ALPHA
            )
        }
        return if (isEdgeToEdgeEnabled) {
            Color.TRANSPARENT
        } else MaterialColors.getColor(
            context,
            android.R.attr.navigationBarColor,
            Color.BLACK
        )
    }

    fun getLiveData(): SPrefBooleanLiveData {
        return SPrefBooleanLiveData(
            sPrefManager,
            KEY_EDGE_TO_EDGE_ENABLED,
            VALUE_DEFAULT_EDGE_TO_EDGE
        )
    }


}