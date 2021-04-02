package com.pramod.dailyword.framework.transition

import android.app.Activity
import android.app.SharedElementCallback
import android.transition.Transition

fun Activity.removeCallbacks(transitionListener: Transition.TransitionListener) {
    window.sharedElementExitTransition.removeListener(transitionListener)
    setExitSharedElementCallback(null as SharedElementCallback?)
}