package com.pramod.dailyword.framework.ui.common.bindingadapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar
import com.pramod.dailyword.R
import com.pramod.dailyword.framework.helper.RightDrawableOnTouchListener
import com.pramod.dailyword.framework.util.CommonUtils


object CommonBindindAdapters {
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
    fun switchingText(textView: TextView, text: SpannableString?) {
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
                    //Log.i("TEXT", it)
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

    @JvmStatic
    @BindingAdapter("hapticVibrate")
    public fun hapticVibrate(view: View, vibrate: Boolean) {
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

    @JvmStatic
    @BindingAdapter(value = ["copyToClipBoardText", "rootLayout"], requireAll = true)
    fun copyToClipBoardOnLongClick(
        textView: TextView,
        copyToClipBoardText: CharSequence?,
        rootLayout: View
    ) {
        textView.setOnLongClickListener {
            CommonUtils.copyToClipboard(
                textView.context,
                copyToClipBoardText!!
            ) {
                Snackbar.make(rootLayout, "Copied to clipboard", Snackbar.LENGTH_SHORT)
                    .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                    .show()
            }
            return@setOnLongClickListener true
        }
    }

    @JvmStatic
    @BindingAdapter("app:onLongClick", requireAll = false)
    fun onLongClick(view: View, onLongClickListener: OnLongClickListener?) {
        view.setOnLongClickListener { it ->
            onLongClickListener?.onLongClick()
            true
        }
    }

    interface OnLongClickListener {
        fun onLongClick()
    }

    @SuppressLint("ClickableViewAccessibility")
    @JvmStatic
    @BindingAdapter("app:onRightDrawableClick")
    fun onRightDrawableClick(
        textView: TextView,
        onTextDrawableClickListener: OnTextDrawableClickListener?
    ) {
        textView.setOnTouchListener(object : RightDrawableOnTouchListener(textView) {
            override fun onDrawableTouch(event: MotionEvent?): Boolean {
                onTextDrawableClickListener?.onRightDrawableClick()
                hapticVibrate(textView, true)
                event?.action = MotionEvent.ACTION_CANCEL;
                return false
            }
        })
    }


    interface OnTextDrawableClickListener {
        fun onRightDrawableClick()
    }


    @BindingAdapter("app:textSwitcherSwitchText")
    fun switchText(switcher: TextSwitcher, text: SpannableString?) {
        if (text == null) return
        val tag = switcher.getTag(R.id.animText_tag) as String?
        if (tag == null || !equals(tag, text)) {
            switcher.setText(text)
            switcher.setTag(R.id.animText_tag, tag)
        }
    }


    @JvmStatic
    @BindingAdapter(
        value = ["app:loadImageUri", "app:loadImageDrawable", "app:placeHolderDrawable", "app:errorDrawable"],
        requireAll = false
    )
    fun loadImageUrl(
        imageView: ImageView,
        imageUri: String?,
        imageDrawable: Drawable?,
        placeHolderDrawable: Drawable?,
        errorDrawable: Drawable?
    ) {
        if (imageDrawable != null) {
            Glide.with(imageView.context)
                .load(imageDrawable)
                .into(imageView)
            return
        }

        if (imageUri == null) {
            Glide.with(imageView.context)
                .load(placeHolderDrawable)
                .into(imageView)
            return
        }

        Glide.with(imageView.context)
            .load(imageUri)
            .placeholder(placeHolderDrawable)
            .error(errorDrawable)
            .transition(DrawableTransitionOptions.withCrossFade(100))
            .into(imageView)

    }


    fun equals(a: Any?, b: Any?): Boolean {
        return if (a == null) b == null else a == b
    }
}

