package com.pramod.dailyword.framework.haptics

import android.os.Vibrator

interface HapticFeedbackManager {

    val deviceVibrator: Vibrator?

    fun perform(type: HapticType)


}
