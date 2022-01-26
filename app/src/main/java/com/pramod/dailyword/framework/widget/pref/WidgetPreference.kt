package com.pramod.dailyword.framework.widget.pref

import android.content.Context
import androidx.annotation.IntRange
import com.google.gson.Gson
import com.pramod.dailyword.framework.prefmanagers.BasePreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class Controls(val label: String) {
    BOOKMARK("Bookmark"),
    RANDOM_WORD("Random Word")
}

@Singleton
class WidgetPreference @Inject constructor(
    @ApplicationContext context: Context
) : BasePreferenceManager(PREF_NAME, context) {

    data class WidgetSize(val width: Int, val height: Int)

    companion object {
        const val PREF_NAME = "widget"
        const val KEY_WIDGET_SIZE = "widget_size"
        const val KEY_CURRENT_WORD_SHOWN = "current_word_shown"

        const val KEY_VISIBLE_WIDGET_CONTROL = "visible_widget_control"
        const val KEY_WIDGET_BODY_BACKGROUND_ALPHA = "widget_body_background_alpha"
        const val KEY_WIDGET_BACKGROUND_ALPHA = "widget_background_alpha"

        const val DEFAULT_WIDGET_BODY_BACKGROUND_ALPHA = 80
        const val DEFAULT_WIDGET_BACKGROUND_ALPHA = 90
    }

    fun setWidgetSize(widgetSize: WidgetSize) {
        editor.putString(KEY_WIDGET_SIZE, Gson().toJson(widgetSize)).apply()
    }

    fun getWidgetSize(): WidgetSize? {
        return sPrefManager.getString(KEY_WIDGET_SIZE, null)?.let {
            Gson().fromJson(it, WidgetSize::class.java)
        }
    }

    fun setCurrentWordShown(wordName: String) {
        editor.putString(KEY_CURRENT_WORD_SHOWN, wordName).apply()
    }

    fun getCurrentWordShown(): String? {
        return sPrefManager.getString(KEY_CURRENT_WORD_SHOWN, null)
    }

    fun getVisibleWidgetControls(): Set<String> {
        return sPrefManager.getStringSet(
            KEY_VISIBLE_WIDGET_CONTROL,
            setOf(Controls.BOOKMARK.label, Controls.RANDOM_WORD.label)
        ) ?: setOf()
    }

    fun setVisibleWidgetControls(controls: Set<String>) {
        editor.putStringSet(KEY_VISIBLE_WIDGET_CONTROL, controls).apply()
    }

    fun clearVisibleWidgetControls() {
        editor.remove(KEY_VISIBLE_WIDGET_CONTROL).apply()
    }

    fun getWidgetBodyAlpha(): Int {
        return sPrefManager.getInt(
            KEY_WIDGET_BODY_BACKGROUND_ALPHA,
            DEFAULT_WIDGET_BODY_BACKGROUND_ALPHA
        )
    }

    fun setWidgetBodyAlpha(@IntRange(from = 0, to = 100) alpha: Int) {
        editor.putInt(KEY_WIDGET_BODY_BACKGROUND_ALPHA, alpha).apply()
    }

    fun getWidgetBackgroundAlpha(): Int {
        return sPrefManager.getInt(
            KEY_WIDGET_BACKGROUND_ALPHA,
            DEFAULT_WIDGET_BACKGROUND_ALPHA
        )
    }

    fun setWidgetBackgroundAlpha(@IntRange(from = 50, to = 100) alpha: Int) {
        editor.putInt(KEY_WIDGET_BACKGROUND_ALPHA, alpha).apply()
    }


    fun removeAll() {
        sPrefManager.edit().clear().apply()
    }
}