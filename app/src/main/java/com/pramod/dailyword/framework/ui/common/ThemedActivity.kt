package com.pramod.dailyword.framework.ui.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pramod.dailyword.framework.prefmanagers.EdgeToEdgeApplicator
import com.pramod.dailyword.framework.prefmanagers.EdgeToEdgeEnabler
import com.pramod.dailyword.framework.prefmanagers.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class ThemedActivity : AppCompatActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var edgeToEdgeApplicator: EdgeToEdgeApplicator

    @Inject
    lateinit var edgeToEdgeEnabler: EdgeToEdgeEnabler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shouldApplyEdgeToEdge()
    }

    private fun shouldApplyEdgeToEdge() {
        edgeToEdgeApplicator.applyForActivity(this)
    }
}