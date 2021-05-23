package com.pramod.dailyword.framework.ui.common.exts

import android.util.Log
import java.util.*

fun Calendar.isSunday(): Boolean {
    Log.i("TAG", "isSunday: " + get(Calendar.DAY_OF_WEEK))
    return get(Calendar.DAY_OF_WEEK) == 1
}

fun Calendar.make12AMInstance() {
    set(Calendar.HOUR_OF_DAY, 12)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}
