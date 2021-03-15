package com.pramod.dailyword.framework.transition

import android.transition.Transition

abstract class TransitionCallback : Transition.TransitionListener {
    override fun onTransitionStart(transition: Transition) {
        // no-op
    }

    override fun onTransitionEnd(transition: Transition) {
        // no-op
    }

    override fun onTransitionCancel(transition: Transition) {
        // no-op
    }

    override fun onTransitionPause(transition: Transition) {
        // no-op
    }

    override fun onTransitionResume(transition: Transition) {
        // no-op
    }
}