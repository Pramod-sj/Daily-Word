package com.pramod.dailyword.helper

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextSwitcher
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.databinding.BindingAdapter
import com.pramod.dailyword.R

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
        @BindingAdapter("switchingText")
        fun switchingText(textView: TextView, text: String?) {
            text?.let {
                if (textView.text == text) {
                    textView.text = text
                    return
                }
                val anim = AlphaAnimation(1f, 0f)
                anim.duration = 200L
                anim.repeatCount = 1
                anim.repeatMode = Animation.REVERSE
                anim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {
                        Log.i("TEXT", it)
                        textView.text = it
                    }

                    override fun onAnimationEnd(animation: Animation?) {

                    }

                    override fun onAnimationStart(animation: Animation?) {
                    }

                })
                textView.startAnimation(anim)
            }
        }

        @JvmStatic
        @BindingAdapter("switchText")
        fun switchText(textView: TextView, text: String?) {
            Log.d("TEXT", textView.text.toString() + "==" + text + ":TEXT")
            if (text == null) {
                return
            }
            if (textView.text == text) {
                textView.text = text
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
            })
            textFadeOut.start()
        }

        @BindingAdapter("hapticVibrate")
        fun hapticVibrate(view: View, vibrate: Boolean) {
            if (vibrate) view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }


        @JvmStatic
        @BindingAdapter("applyTextSwitcherFadeAnim")
        fun applyFadeAnimation(view: TextSwitcher, duration: Long) {
            val fadeIn = AnimationUtils.loadAnimation(view.context, android.R.anim.fade_in)
            fadeIn.duration = duration
            val fadeOut = AnimationUtils.loadAnimation(view.context, android.R.anim.fade_out)
            fadeOut.duration = duration
            view.inAnimation = fadeIn
            view.outAnimation = fadeOut
        }
    }
}

