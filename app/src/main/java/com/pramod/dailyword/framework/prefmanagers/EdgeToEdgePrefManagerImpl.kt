package com.pramod.dailyword.framework.prefmanagers

import android.content.Context
import android.graphics.Color
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import androidx.lifecycle.LiveData
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class EdgeToEdgePrefManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BasePreferenceManager(PREFERENCES_NAME, context), EdgeToEdgeEnabler, EdgeToEdgeApplicator {

    companion object {
        val TAG = EdgeToEdgePrefManagerImpl::class.java.simpleName

        private const val VALUE_DEFAULT_EDGE_TO_EDGE = true

        private const val PREFERENCES_NAME = "window_preferences"
        private const val KEY_EDGE_TO_EDGE_ENABLED = "edge_to_edge_enabled"
        private const val EDGE_TO_EDGE_BAR_ALPHA = 128

    }

    override val isEnabled: Boolean
        get() = sPrefManager.getBoolean(
            KEY_EDGE_TO_EDGE_ENABLED,
            VALUE_DEFAULT_EDGE_TO_EDGE
        ) || VERSION.SDK_INT >= VERSION_CODES.VANILLA_ICE_CREAM

    override val isEnabledLiveData: LiveData<Boolean> = SPrefBooleanLiveData(
        sPrefManager,
        KEY_EDGE_TO_EDGE_ENABLED,
        VALUE_DEFAULT_EDGE_TO_EDGE
    )

    override fun toggle() {
        editor.putBoolean(KEY_EDGE_TO_EDGE_ENABLED, !isEnabled).commit()
    }

    override fun applyForActivity(activity: ComponentActivity) {
        if (isEnabled) {
            activity.enableEdgeToEdge()
            activity.window.isNavigationBarContrastEnforced = true
        }
    }

    override fun applyForDialog(window: Window) {
        WindowCompat.setDecorFitsSystemWindows(window, !isEnabled)
        window.navigationBarColor = getNavBarColor(isEnabled, null)
    }

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
}

interface EdgeToEdgeEnabler {

    /**
     * edge to edge is turned on by default
     */
    val isEnabled: Boolean

    val isEnabledLiveData: LiveData<Boolean>

    fun toggle()

}

interface EdgeToEdgeApplicator {

    fun applyForActivity(activity: ComponentActivity)

    fun applyForDialog(window: Window)

}