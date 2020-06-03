package com.pramod.todaysword.helper

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.databinding.BindingAdapter
import com.pramod.todaysword.R

class CommonBindindAdapters {
    companion object {
        @JvmStatic
        @BindingAdapter("loadDrawable")
        fun loadDrawable(imageView: ImageView, drawable: Drawable) {
            imageView.setImageDrawable(drawable)
        }

        @JvmStatic
        @BindingAdapter("loadDrawable")
        fun loadDrawable(imageView: ImageView, drawableRes: Int) {
            imageView.setImageResource(drawableRes)
        }


        @JvmStatic
        @BindingAdapter("switchText")
        fun switchText(textView: TextView, text: String?) {
            if (text == null) {
                return
            }
            if (textView.text == text) {
                return
            }
            val colorStateList = textView.textColors
            val textColor = textView.textColors.getColorForState(
                textView.drawableState,
                ContextCompat.getColor(textView.context, R.color.grey_extra_dark)
            )
            val str = "textColor"
            val textFadeOut = ObjectAnimator.ofArgb(
                textView,
                str,
                textColor,
                ColorUtils.setAlphaComponent(textColor, 0)
            ).setDuration(200)
            val textFadeIn = ObjectAnimator.ofArgb(
                textView,
                str,
                ColorUtils.setAlphaComponent(textColor, 0),
                textColor
            ).setDuration(200)
            textFadeOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    textView.text = text
                    textFadeIn.start()
                }
            })
            textFadeIn.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                }
            })
            textFadeOut.start()
        }

        @JvmStatic
        @BindingAdapter("switchSpannableText")
        fun switchText(textView: TextView, text: SpannableString?) {
            if (text == null) {
                return
            }
            if (textView.text == text) {
                return
            }
            val colorStateList = textView.textColors
            val textColor = textView.textColors.getColorForState(
                textView.drawableState,
                ContextCompat.getColor(textView.context, R.color.grey_extra_dark)
            )
            val str = "textColor"
            val textFadeOut = ObjectAnimator.ofArgb(
                textView,
                str,
                textColor,
                ColorUtils.setAlphaComponent(textColor, 0)
            ).setDuration(200)
            val textFadeIn = ObjectAnimator.ofArgb(
                textView,
                str,
                ColorUtils.setAlphaComponent(textColor, 0),
                textColor
            ).setDuration(200)
            textFadeOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    textView.text = text
                    textFadeIn.start()
                }
            })
            textFadeIn.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                }
            })
            textFadeOut.start()
        }

        @BindingAdapter("hapticVibrate")
        fun hapticVibrate(view: View, vibrate: Boolean) {
            if (vibrate) view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
}

