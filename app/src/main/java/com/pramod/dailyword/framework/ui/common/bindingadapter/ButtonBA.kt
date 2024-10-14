package com.pramod.dailyword.framework.ui.common.bindingadapter

import android.content.res.ColorStateList
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import com.pramod.dailyword.R
import com.pramod.dailyword.business.domain.model.Word
import com.pramod.dailyword.framework.prefmanagers.ThemeManager
import com.pramod.dailyword.framework.util.CommonUtils

object ButtonBA {
    @JvmStatic
    @BindingAdapter("applyBackgroundTint")
    fun applyBackgroundTint(materialButton: MaterialButton, color: Int) {
        materialButton.backgroundTintList = ColorStateList.valueOf(color)
    }

    @JvmStatic
    @BindingAdapter("app:buttonTextColorBasedOnWord")
    fun setButtonTextColor(button: MaterialButton, word: Word?) {
        if (word != null && word.wordColor != -1 && word.wordDesaturatedColor != -1) {
            val color = if (!ThemeManager.isNightModeActive(button.context)) {
                CommonUtils.getColor(button.context, word.wordColor)
            } else {
                CommonUtils.getColor(button.context, word.wordDesaturatedColor)
            }
            setButtonColor(
                button,
                color
            )
        } else {
            val color = CommonUtils.getColor(button.context, R.color.colorPrimary)
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