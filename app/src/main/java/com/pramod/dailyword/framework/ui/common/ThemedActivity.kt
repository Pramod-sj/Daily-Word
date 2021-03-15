package com.pramod.dailyword.framework.ui.common

import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import com.pramod.dailyword.R
import com.pramod.dailyword.databinding.LayoutCustomTitleToolbarBinding
import com.pramod.dailyword.framework.prefmanagers.EgdeToEdgePrefManager
import com.pramod.dailyword.framework.prefmanagers.ThemeManager
import com.pramod.dailyword.framework.prefmanagers.WindowAnimPrefManager
import com.pramod.dailyword.framework.util.CommonUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class ThemedActivity : AppCompatActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var egdeToEdgePrefManager: EgdeToEdgePrefManager

    @Inject
    lateinit var windowAnimPrefManager: WindowAnimPrefManager

    private var forceEdgeToEdge: Boolean? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        shouldApplyEdgeToEdge()
        super.onCreate(savedInstanceState)
    }

    fun forceEdgeToEdge(forceEdgeToEdge: Boolean) {
        this.forceEdgeToEdge = forceEdgeToEdge
    }

    private fun shouldApplyEdgeToEdge() {
        egdeToEdgePrefManager.applyEdgeToEdgeIfEnabled(window, forceEdgeToEdge)
    }

    open fun lightStatusBar(
        makeLight: Boolean = !ThemeManager.isNightModeActive(this),
        matchingBackgroundColor: Boolean = true
    ) {
        configStatus(makeLight, -1, matchingBackgroundColor)
    }

    open fun lightStatusBar(makeLight: Boolean, statusBarColorResId: Int) {
        configStatus(makeLight, statusBarColorResId, false)
    }

    open fun lightStatusBar(makeLight: Boolean) {
        configStatus(makeLight, -1, false)
    }


    private fun configStatus(
        makeLight: Boolean,
        statusBarColorResId: Int,
        matchingBackgroundColor: Boolean
    ) {
        Log.i("BASE ACTIVITY", makeLight.toString())
        val oldFlags = window.decorView.systemUiVisibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = oldFlags
            flags = if (makeLight) {
                flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            window.decorView.systemUiVisibility = flags
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            if (matchingBackgroundColor) {
                window.statusBarColor =
                    CommonUtils.resolveAttrToColor(this, android.R.attr.windowBackground)
            } else {
                window.statusBarColor = ContextCompat.getColor(
                    this,
                    if (statusBarColorResId != -1) statusBarColorResId
                    else (if (makeLight) R.color.white else R.color.black)
                )
            }
            Log.i("STATUS BAR COLOR", window.statusBarColor.toString())
        } else {
            window.statusBarColor = resources.getColor(com.pramod.dailyword.R.color.black)
        }
    }
}