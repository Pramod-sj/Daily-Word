package com.pramod.dailyword.binding_adapters

import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import com.pramod.dailyword.R
import com.pramod.dailyword.db.model.WordOfTheDay
import com.pramod.dailyword.helper.ThemeManager
import com.pramod.dailyword.util.CommonUtils

object ButtonBA {
    @JvmStatic
    @BindingAdapter("applyBackgroundTint")
    fun applyBackgroundTint(materialButton: MaterialButton, color: Int) {
        materialButton.backgroundTintList = ColorStateList.valueOf(color)
    }

    @JvmStatic
    @BindingAdapter("app:buttonTextColorBasedOnWord")
    fun setButtonTextColor(button: MaterialButton, wordOfTheDay: WordOfTheDay?) {
        if (wordOfTheDay != null) {
            val color = if (!ThemeManager.isNightModeActive(button.context)) {
                ContextCompat.getColor(button.context, wordOfTheDay.wordDesaturatedColor)
            } else {
                ContextCompat.getColor(button.context, wordOfTheDay.wordDesaturatedColor)
            }
            setButtonColor(
                button,
                color
            )
        } else {
            val color = ContextCompat.getColor(button.context, R.color.colorPrimary)
            setButtonColor(
                button,
                color
            )
        }
    }

    @JvmStatic
    private fun setButtonColor(button: MaterialButton, color: Int) {
        button.setTextColor(color)
        button.iconTint = ColorStateList.valueOf(color)
        button.rippleColor = ColorStateList.valueOf(CommonUtils.changeAlpha(color, 30))
    }


}