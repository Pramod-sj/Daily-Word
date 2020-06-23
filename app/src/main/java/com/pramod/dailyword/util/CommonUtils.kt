package com.pramod.dailyword.util

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.pramod.dailyword.BuildConfig
import com.pramod.dailyword.R
import java.util.*
import kotlin.collections.map as map1

class CommonUtils {
    companion object {
        @JvmStatic
        fun formatString(list: List<String>, delimiter: String): String {
            val stringBuilder: StringBuilder = StringBuilder()
            for (value in list) {
                stringBuilder.append(value)
                stringBuilder.append("\n")
            }
            return stringBuilder.toString()
        }

        @JvmStatic
        fun getGreetMessage(): String {
            val cal: Calendar = Calendar.getInstance()
            return when (cal.get(Calendar.HOUR_OF_DAY)) {
                in 0..11 -> "Good Morning"
                in 12..16 -> "Good Afternoon"
                else -> "Good Evening"
            }
        }

        @JvmStatic
        fun getColoredSpannedText(
            context: Context,
            text: String,
            from: Int,
            to: Int,
            colorResId: Int
        ): SpannableString {
            val spannableString = SpannableString(text)
            spannableString.setSpan(
                ForegroundColorSpan(colorResId),
                from,
                to,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannableString
        }


        @JvmStatic
        fun formattedListToString(values: List<String>, delimiter: String): String {
            val builder = StringBuilder()
            for (value in values) {
                builder.append(value)
                builder.append(delimiter)
            }
            return builder.toString()
        }

        @JvmStatic
        fun copyToClipboard(
            context: Context,
            text: CharSequence,
            listener: ClipboardManager.OnPrimaryClipChangedListener? = null
        ) {
            val clipboard: ClipboardManager =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.addPrimaryClipChangedListener(listener)
            val clip = ClipData.newPlainText("label", text)
            clipboard.setPrimaryClip(clip)
        }

        @JvmStatic
        fun getAutoTimeTheme(): Int {
            val calendar = Calendar.getInstance()
            return when (calendar.get(Calendar.HOUR_OF_DAY)) {
                in 0..5 -> AppCompatDelegate.MODE_NIGHT_YES
                in 6..18 -> AppCompatDelegate.MODE_NIGHT_NO
                in 19..24 -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_NO
            }
        }

        @JvmStatic
        fun resolveAttrToColor(context: Context, attr: Int): Int {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(attr, typedValue, true)
            return typedValue.data
        }

/*
        fun createWorkerRequest(
            workerClass: Class<out ListenableWorker>,
            scheduleAtCalendar: Calendar?,
            retryTimeInMinutes: Long = 30
        ): OneTimeWorkRequest {

            val contraints: Constraints =
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val oneTimeWorkRequestBuilder = OneTimeWorkRequest.Builder(workerClass)
                .setBackoffCriteria(BackoffPolicy.LINEAR, retryTimeInMinutes, TimeUnit.MINUTES)
                .setConstraints(contraints)
            scheduleAtCalendar?.let {
                var initialDelay =
                    scheduleAtCalendar.timeInMillis - Calendar.getInstance().timeInMillis
                if (initialDelay < 0) {
                    scheduleAtCalendar.roll(Calendar.DATE, true)
                    initialDelay =
                        scheduleAtCalendar.timeInMillis - Calendar.getInstance().timeInMillis
                }
                Log.i("INITITAL DELAY", initialDelay.toString())
                oneTimeWorkRequestBuilder.setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            }
            return oneTimeWorkRequestBuilder.build()
        }*/


        fun isAtLeastAndroidP() = Build.VERSION.SDK_INT >= 28


        fun isAtLeastAndroidL() = Build.VERSION.SDK_INT >= 21

        fun scaleXY(
            view: View, startX: Float, startY: Float, endX: Float, endY: Float,
            duration: Long = 1000,
            animStartCallback: () -> Unit,
            animEndCallback: () -> Unit
        ) {
            val scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 2.5f, 1f)
            scaleXAnimator.interpolator = AccelerateDecelerateInterpolator()
            scaleXAnimator.duration = duration
            val scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 2.5f, 1f)
            scaleYAnimator.interpolator = AccelerateDecelerateInterpolator()
            scaleYAnimator.duration = duration
            val animator = AnimatorSet()
            animator.playTogether(scaleXAnimator, scaleYAnimator)
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onAnimationEnd(animation: Animator?) {
                    animEndCallback.invoke()
                }

                override fun onAnimationCancel(animation: Animator?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onAnimationStart(animation: Animator?) {
                    animStartCallback.invoke()
                }

            })
            animator.start()
        }

        @JvmStatic
        fun capitalizeFirstLetter(text: String): String {
            return text.split(" ").map1 {
                it.toLowerCase(Locale.getDefault()).capitalize()
            }.joinToString()
        }

        @JvmStatic
        fun changeAlpha(color: Int, alpha: Int) = ColorUtils.setAlphaComponent(color, alpha)

        @JvmStatic
        fun changeAlpha(context: Context, color: Int, alpha: Int) =
            ColorUtils.setAlphaComponent(ContextCompat.getColor(context, color), alpha)

        @JvmStatic
        fun pixelToSp(context: Context, pixel: Float): Float =
            pixel / context.resources.displayMetrics.scaledDensity


        @JvmStatic
        fun pixelToDp(context: Context, pixel: Float): Float =
            pixel / context.resources.displayMetrics.density

        @JvmStatic
        fun dpToPixel(context: Context, dp: Float): Float =
            dp * context.resources.displayMetrics.density * 0.5f


        @JvmStatic
        fun calculateActionBarHeight(context: Context): Int {
            val typedValue = TypedValue()
            if (context.theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
                return TypedValue.complexToDimensionPixelSize(
                    typedValue.data,
                    context.resources.displayMetrics
                )
            }
            return 0
        }

        @JvmStatic
        fun getColorBasedOnDay(cal: Calendar?): List<Int> {

            return when (cal?.get(Calendar.DAY_OF_WEEK)
                ?: Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> arrayListOf(R.color.color_mon, R.color.desaturated_color_mon)
                Calendar.TUESDAY -> arrayListOf(R.color.color_tue, R.color.desaturated_color_tue)
                Calendar.WEDNESDAY -> arrayListOf(R.color.color_wed, R.color.desaturated_color_wed)
                Calendar.THURSDAY -> arrayListOf(R.color.color_thur, R.color.desaturated_color_thur)
                Calendar.FRIDAY -> arrayListOf(R.color.color_fri, R.color.desaturated_color_fri)
                Calendar.SATURDAY -> arrayListOf(R.color.color_sat, R.color.desaturated_color_sat)
                Calendar.SUNDAY -> arrayListOf(R.color.color_sun, R.color.desaturated_color_sun)
                else -> arrayListOf(-1, -1)
            }

        }

    }

}