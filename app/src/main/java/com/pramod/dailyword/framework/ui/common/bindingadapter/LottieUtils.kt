package com.pramod.dailyword.framework.ui.common.bindingadapter

import android.util.Log
import androidx.databinding.BindingAdapter
import com.airbnb.lottie.LottieAnimationView

object LottieUtils {

    @JvmStatic
    @BindingAdapter("app:lottie_animate", requireAll = false)
    fun play(lottieAnimationView: LottieAnimationView, animate: Boolean?) {
        Log.i("LottieUtils", "play: null check pending $animate")
        animate?.let {
            Log.i("LottieUtils", "play: $it")
            if (it) {
                lottieAnimationView.playAnimation()
            } else {
                lottieAnimationView.cancelAnimation()
            }
        }
    }
}