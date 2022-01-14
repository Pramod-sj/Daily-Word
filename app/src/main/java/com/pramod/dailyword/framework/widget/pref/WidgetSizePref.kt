package com.pramod.dailyword.framework.widget.pref

import android.content.Context
import com.google.gson.Gson
import com.pramod.dailyword.framework.prefmanagers.BasePreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetSizePref @Inject constructor(
    @ApplicationContext context: Context
) : BasePreferenceManager(PREF_NAME, context) {

    data class WidgetSize(val width: Int, val height: Int)

    companion object {
        const val PREF_NAME = "widget"
        const val KEY_WIDGET_SIZE = "widget_size"
    }

    fun setWidgetSize(widgetSize: WidgetSize) {
        editor.putString(KEY_WIDGET_SIZE, Gson().toJson(widgetSize)).apply()
    }

    fun getWidgetSize(): WidgetSize? {
        return sPrefManager.getString(KEY_WIDGET_SIZE, null)?.let {
            Gson().fromJson(it, WidgetSize::class.java)
        }
    }

}