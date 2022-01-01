package com.pramod.dailyword.framework.ui.common.bindingadapter

import androidx.databinding.BindingAdapter
import com.airbnb.lottie.LottieAnimationView
import timber.log.Timber

object LottieUtils {

    @JvmStatic
    @BindingAdapter("app:lottie_animate", requireAll = false)
    fun play(lottieAnimationView: LottieAnimationView, animate: Boolean?) {
        Timber.i("LottieUtils", "play: null check pending $animate")
        animate?.let {
            Timber.i("LottieUtils", "play: $it")
            if (it) {
                lottieAnimationView.playAnimation()
            } else {
                lottieAnimationView.cancelAnimation()
            }
        }
    }
}