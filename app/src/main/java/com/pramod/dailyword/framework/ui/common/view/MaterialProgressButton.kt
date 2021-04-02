package com.pramod.dailyword.framework.ui.common.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.DynamicDrawableSpan
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.android.material.button.MaterialButton
import com.pramod.dailyword.R

class MaterialProgressButton : MaterialButton {
    private val TEXT_FADE_ANIMATION_DURATION: Long = 200
    private var buttonText: String? = null
    private var circularProgressDrawable: CircularProgressDrawable? = null
    private var isShowingProgress = false

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        buttonText = text.toString()
        circularProgressDrawable =
            CircularProgressDrawable(context).mutate() as CircularProgressDrawable
        circularProgressDrawable!!.setColorSchemeColors(getColorByAttrId(R.attr.colorPrimary))
        circularProgressDrawable!!.strokeCap = Paint.Cap.ROUND
        circularProgressDrawable!!.setStyle(CircularProgressDrawable.DEFAULT)
        val size =
            (circularProgressDrawable!!.centerRadius + circularProgressDrawable!!.strokeWidth).toInt() * 2
        circularProgressDrawable!!.setBounds(0, 0, size, size)
    }

    fun showProgress(show: Boolean?) {
        Log.i(TAG, "showProgress: $isEnabled")
        if (show == null) {
            return
        }
        isShowingProgress = show
        isEnabled = !show
        if (show) {
            val progressSpanString = SpannableString(" ")
            val drawableSpan: DynamicDrawableSpan = object : DynamicDrawableSpan() {
                override fun getDrawable(): Drawable {
                    return circularProgressDrawable!!
                }
            }
            circularProgressDrawable!!.callback = progressCallback
            circularProgressDrawable!!.start()
            progressSpanString.setSpan(drawableSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            text = progressSpanString
        } else {
            circularProgressDrawable!!.stop()
            circularProgressDrawable!!.callback = null
            val buttonTextSpan = SpannableString(buttonText)
            text = buttonTextSpan
        }
    }

    override fun setEnabled(enabled: Boolean) {
        if (isShowingProgress) {
            super.setEnabled(false)
        } else {
            super.setEnabled(enabled)
        }
    }

    private fun setTextUsingFadeAnimation(spannableString: SpannableString) {
        //text fading animation
        //this is to retain color state
        val colorStateList = textColors
        val textColor = textColors.getColorForState(
            drawableState, ContextCompat.getColor(
                context, android.R.color.white
            )
        )
        val textFadeOut = ObjectAnimator.ofArgb(
            this,
            "textColor",
            textColor,
            ColorUtils.setAlphaComponent(textColor, 0)
        )
            .setDuration(TEXT_FADE_ANIMATION_DURATION)
        val textFadeIn = ObjectAnimator.ofArgb(
            this,
            "textColor",
            ColorUtils.setAlphaComponent(textColor, 0),
            textColor
        )
            .setDuration(TEXT_FADE_ANIMATION_DURATION)
        textFadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                text = spannableString
                textFadeIn.start()
            }
        })
        textFadeIn.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                //setting the color state
                setTextColor(colorStateList)
            }
        })
        textFadeOut.start()
    }
    /*@Override
    public void setEnabled(boolean enabled) {
        setEnabled(enabled, false);
    }*/
    /*public void setEnabled(boolean enabled, boolean animate) {
        if (animate) {
            //background fading animation
            ColorStateList backgroundColorList = getBackgroundTintList();
            int enabledBackgroundColor = getBackgroundTintList().getColorForState(ENABLED_STATE_SET, getColorByAttrId(R.attr.colorPrimary));
            int disabledBackgroundColor = ColorUtils.setAlphaComponent(getColorByAttrId(R.attr.colorOnSurface), (int) (0.18f * 255f));
            ObjectAnimator backgroundEnabledToDisabledFade = ObjectAnimator.ofArgb(this, "backgroundColor", enabledBackgroundColor, disabledBackgroundColor).setDuration(50);
            ObjectAnimator backgroundDisabledToEnabledFade = ObjectAnimator.ofArgb(this, "backgroundColor", disabledBackgroundColor, enabledBackgroundColor).setDuration(50);
            if (enabled) {
                backgroundDisabledToEnabledFade.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        setBackgroundTintList(backgroundColorList);
                    }
                });
                backgroundDisabledToEnabledFade.start();
            } else {
                backgroundEnabledToDisabledFade.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        setBackgroundTintList(backgroundColorList);
                    }
                });
                backgroundEnabledToDisabledFade.start();
            }
        }
        super.setEnabled(enabled);
    }*/
    /**
     * This callback is use to call material button onDraw() method by calling invalidate() of MaterialButton
     * inside invalidateDrawable() method of progress drawable
     */
    private val progressCallback: Drawable.Callback = object : Drawable.Callback {
        override fun invalidateDrawable(who: Drawable) {
            this@MaterialProgressButton.invalidate()
        }

        override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {}
        override fun unscheduleDrawable(who: Drawable, what: Runnable) {}
    }

    private fun getColorByAttrId(attrId: Int): Int {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(attrId, typedValue, true)
        return typedValue.data
    }

    companion object {
        private val TAG = MaterialProgressButton::class.java.simpleName
    }
}