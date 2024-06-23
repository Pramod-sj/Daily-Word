package com.pramod.dailyword.framework.util

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.telephony.TelephonyManager
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.annotation.FloatRange
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.text.toSpannable
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.pramod.dailyword.R
import com.pramod.dailyword.framework.prefmanagers.ThemeManager
import com.pramod.dailyword.framework.ui.common.exts.getLocalCalendar
import com.pramod.dailyword.framework.ui.common.exts.resolveAttrToColor
import timber.log.Timber
import java.util.Calendar
import java.util.Locale
import kotlin.collections.map as map1


object CommonUtils {

    fun formatListAsBulletList(items: List<String>): Spannable {
        val spannableStringBuilder = SpannableStringBuilder()
        items.forEachIndexed { index, s ->
            spannableStringBuilder.append(
                s, BulletSpan(15), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (index < items.size - 1) {
                spannableStringBuilder.appendLine()
            }
        }
        return spannableStringBuilder.toSpannable()
    }

    fun viewToBitmap(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) bgDrawable.draw(canvas) else canvas.drawColor(
            view.context.resolveAttrToColor(
                android.R.attr.colorBackground
            )
        )
        view.draw(canvas)
        return returnedBitmap
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        var width = drawable.intrinsicWidth
        width = if (width > 0) width else 1
        var height = drawable.intrinsicHeight
        height = if (height > 0) height else 1
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

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
    fun getFancyAppName(context: Context): SpannableString {
        val appname = "Daily Word"
        val span = SpannableString(appname)
        span.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorPrimary)),
            6,
            appname.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        span.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            appname.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return span
    }

    @JvmStatic
    fun getGreetMessage(context: Context): SpannableString {
        val cal: Calendar = getLocalCalendar()

        val random = arrayOf(
            context.getString(R.string.greeting_hi),
            context.getString(R.string.greeting_hey),
            context.getString(R.string.greeting_hello),
            ""
        )

        return SpannableString(
            when (cal.get(Calendar.HOUR_OF_DAY)) {
                in 0..11 -> {
                    String.format(
                        context.getString(R.string.greeting_good_morning),
                        random.random()
                    ).trim()
                }

                in 12..16 -> {
                    String.format(
                        context.getString(R.string.greeting_good_afternoon),
                        random.random()
                    ).trim()
                }

                else -> {
                    String.format(
                        context.getString(R.string.greeting_good_evening),
                        random.random()
                    ).trim()
                }
            }
        ).also {
            it.setSpan(RelativeSizeSpan(0.9f), 0, it.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
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
        if (listener != null) {
            clipboard.addPrimaryClipChangedListener(listener)
        }
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)

    }

    @JvmStatic
    fun getAutoTimeTheme(): Int {
        val calendar = getLocalCalendar()
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
        view: View,
        @FloatRange(from = -1.0, to = 1.0) startX: Float,
        @FloatRange(from = -1.0, to = 1.0) startY: Float,
        @FloatRange(from = -1.0, to = 1.0) endX: Float,
        @FloatRange(from = -1.0, to = 1.0) endY: Float,
        duration: Long = 1000,
        animStartCallback: () -> Unit,
        animEndCallback: () -> Unit
    ) {
        val scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", startX, endX)
        scaleXAnimator.interpolator = FastOutSlowInInterpolator()
        scaleXAnimator.duration = duration
        val scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", startY, endY)
        scaleYAnimator.interpolator = FastOutSlowInInterpolator()
        scaleYAnimator.duration = duration
        val alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
        alphaAnimator.interpolator = FastOutSlowInInterpolator()
        alphaAnimator.duration = duration
        val animator = AnimatorSet()
        animator.playTogether(scaleXAnimator, scaleYAnimator, alphaAnimator)
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                animEndCallback.invoke()
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationStart(animation: Animator) {
                animStartCallback.invoke()
            }

        })
        animator.start()
    }

    @JvmStatic
    fun capitalizeFirstLetter(text: String): String {
        return text.split(" ").map1 {
            it.lowercase(Locale.getDefault())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }.joinToString()
    }

    @JvmStatic
    fun changeAlpha(color: Int, alpha: Int) = ColorUtils.setAlphaComponent(color, alpha)


    @JvmStatic
    fun changeAlpha(context: Context, colorResId: Int, alpha: Int) =
        ColorUtils.setAlphaComponent(
            getColor(context, colorResId), alpha
        )

    @JvmStatic
    fun getColor(context: Context, colorResId: Int): Int {
        return ContextCompat.getColor(
            context,
            if (colorResId == -1 || colorResId == 0)
                if (ThemeManager.isNightModeActive(context))
                    R.color.colorPrimaryDesaturated
                else
                    R.color.colorPrimary
            else colorResId
        )

    }


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
            ?: getLocalCalendar().get(Calendar.DAY_OF_WEEK)) {
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


    fun showViewSlide(view: View, duration: Long = 1000) {
        view.isVisible = true
        val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
            view, PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0f, view.width * 1.0f)
        )
        objectAnimator.duration = duration
        objectAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {}

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationStart(animation: Animator) {}

        })
        objectAnimator.start()
    }

    fun hideViewSlide(view: View, duration: Long = 1000) {
        val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
            view, PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.width * 1.0f, 0f)
        )
        objectAnimator.duration = duration
        objectAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                view.isVisible = false
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationStart(animation: Animator) {}

        })
        objectAnimator.start()
    }


    fun showViewAlphaAnimation(view: View, duration: Long = 500) {
        view.isVisible = true
        val alphaAnimation = AlphaAnimation(0f, 1f)
        alphaAnimation.duration = duration
        alphaAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {

            }

            override fun onAnimationStart(animation: Animation?) {

            }

        })
        view.startAnimation(alphaAnimation)
    }

    fun hideViewAlphaAnimation(view: View, duration: Long = 500) {
        val alphaAnimation: AlphaAnimation = AlphaAnimation(1f, 0f)
        alphaAnimation.duration = duration
        alphaAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                view.isVisible = false
            }

            override fun onAnimationStart(animation: Animation?) {

            }

        })
        view.startAnimation(alphaAnimation)
    }


    fun loadJsonFromAsset(context: Context, fileName: String): String? {
        var json: String? = null
        try {
            val inputStream = context.assets.open(fileName)
            json = String(inputStream.readBytes())
        } catch (e: Exception) {
            Timber.e("loadJsonFromAssest", "loadJsonFromAssest: ", e)
            return json
        }
        return json
    }


    fun getCountryCodeFromTelephoneManager(context: Context): String? {

        val telephonyManager: TelephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        return when {
            telephonyManager.simCountryIso != null -> {
                telephonyManager.simCountryIso
            }

            telephonyManager.networkCountryIso != null -> {
                telephonyManager.networkCountryIso
            }

            else -> {
                null
            }
        }

    }


    @JvmStatic
    fun getTopNItemFromList(list: List<String>?, n: Int): List<String> {
        val newList = arrayListOf<String>()
        if (list == null) {
            return newList
        }
        for (i in 0 until n) {
            if (i < list.size) {
                newList.add(list[i])
            }
        }
        return newList
    }


}