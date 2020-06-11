package com.pramod.todaysword.util

import java.text.SimpleDateFormat
import java.util.*

class CalenderUtil {
    companion object {
        const val MERRIAN_DATE_FORMAT = "MMMM d, yyyy"
        const val DATE_FORMAT_DISPLAY = "dd MMM"
        const val DATE_FORMAT = "yyyy-MM-dd"
        const val DATE_TIME_FORMAT = "yyyy-MM-dd hh:mm:ss a"

        @JvmStatic
        fun convertCalenderToString(calender: Calendar, dateFormat: String): String {
            val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.US)
            return simpleDateFormat.format(calender.time)
        }

        @JvmStatic
        fun convertStringToCalender(dateString: String, dateFormat: String): Calendar? {
            val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.US)
            val date = simpleDateFormat.parse(dateString)
            if (date != null) {
                val calender = Calendar.getInstance()
                calender.time = date
                return calender
            }
            return null
        }

        @JvmStatic
        fun convertDateStringToSpecifiedDateString(
            dateString: String,
            dateFormat: String = DATE_FORMAT,
            requiredDateFormat: String = DATE_FORMAT_DISPLAY
        ): String? {
            val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.US)
            val date = simpleDateFormat.parse(dateString)
            if (date != null) {
                val reqSimpleDateFormat = SimpleDateFormat(requiredDateFormat, Locale.US)
                return reqSimpleDateFormat.format(date)
            }
            return null
        }

        @JvmStatic
        fun isYesterday(dateString: String, dateFormat: String = DATE_FORMAT): Boolean {
            val cal: Calendar = Calendar.getInstance()
            cal.roll(Calendar.DATE, false)
            return convertCalenderToString(cal, dateFormat) == dateString
        }

        @JvmStatic
        fun isToday(dateString: String, dateFormat: String = DATE_FORMAT): Boolean {
            val cal: Calendar = Calendar.getInstance()
            return convertCalenderToString(cal, dateFormat) == dateString
        }


        @JvmStatic
        fun createCalendarForHourOfDayAndMin(hourOfDay: Int, minute: Int = 0): Calendar {
            val calender = Calendar.getInstance()
            calender.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calender.set(Calendar.MINUTE, minute)
            calender.set(Calendar.SECOND, 0)
            return calender
        }

        @JvmStatic
        fun getDayFromDateString(date: String, format: String): String {
            val calendar = convertStringToCalender(date, format)
            if (calendar != null) {
                return SimpleDateFormat("dd", Locale.getDefault()).format(calendar.time)
            }
            return date.substring(0, 2);
        }


        @JvmStatic
        fun getMonthFromDateString(date: String, format: String): String {
            val calendar = convertStringToCalender(date, format)
            if (calendar != null) {
                return SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
            }
            return date.substring(2, 5);
        }

    }
}