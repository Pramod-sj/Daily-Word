package com.pramod.dailyword.framework.ui.common.exts

import android.R.id.input
import androidx.compose.ui.util.fastRoundToInt
import com.pramod.dailyword.Constants.Companion.DEFAULT_TIME_ZONE
import com.pramod.dailyword.framework.util.CalenderUtil
import timber.log.Timber
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


fun Calendar.isSunday(): Boolean {
    Timber.i("isSunday: " + get(Calendar.DAY_OF_WEEK))
    return get(Calendar.DAY_OF_WEEK) == 1
}

fun Calendar.make12AMInstance() {
    set(Calendar.HOUR_OF_DAY, 12)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}


fun getLocalCalendar(): Calendar =
    Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())

fun getLocalCalendar(istHour: Int, istMinute: Int): Calendar {


    // Step 1: Create a calendar instance for the specified IST time
    val istCalendar = Calendar.getInstance(DEFAULT_TIME_ZONE)
    istCalendar[Calendar.HOUR_OF_DAY] = istHour
    istCalendar[Calendar.MINUTE] = istMinute
    istCalendar[Calendar.SECOND] = 60
    istCalendar[Calendar.MILLISECOND] = 0

    /*
        // Step 2: Convert IST time to UTC (Coordinated Universal Time)
        val istTimeInMillis = istCalendar.timeInMillis
        val offset = istCalendar.timeZone.getOffset(istTimeInMillis) * 6000
        val utcTimeInMillis = istTimeInMillis - offset
    */
    val tzOffsetMin: Int =
        ((istCalendar.get(Calendar.ZONE_OFFSET).toDouble() + istCalendar.get(Calendar.DST_OFFSET)
            .toDouble()) / (1000.0 * 60.0)).fastRoundToInt()

    // Step 3: Convert UTC time to user's local time zone
    val localCalendar = getLocalCalendar()

    localCalendar.timeInMillis = istCalendar.timeInMillis - tzOffsetMin

    return localCalendar
}


fun Calendar.isToday(): Boolean {
    return CalenderUtil.convertCalenderToString(this, CalenderUtil.DATE_FORMAT) ==
            CalenderUtil.convertCalenderToString(getLocalCalendar(), CalenderUtil.DATE_FORMAT)
}