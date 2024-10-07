package com.pramod.dailyword.framework.util

import com.pramod.dailyword.framework.ui.common.exts.getLocalCalendar
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.List
import kotlin.math.abs

class CalenderUtil {
    companion object {
        const val DATE_FORMAT_DISPLAY = "dd MMM"
        const val DATE_WITH_YEAR_FORMAT_DISPLAY = "dd MMM yy"
        const val DATE_FORMAT = "yyyy-MM-dd"
        const val DATE_TIME_FORMAT = "yyyy-MM-dd hh:mm:ss a"
        const val DATE_TIME_FORMAT_BEAUTIFY = "dd MMM hh:mm a"
        const val TIME_FORMAT = "hh:mm a"

        val DAYS: List<String>
            get() = DateFormatSymbols(Locale.ENGLISH).weekdays.toMutableList()
                .apply {
                    remove("")//remove empty string
                }.toList()

        fun isTodaySunday(calender: Calendar): Boolean {
            return getLocalCalendar().get(Calendar.DAY_OF_WEEK) == 0
        }

        fun getCalendarInstance(as12AM: Boolean = false): Calendar {
            val calendar = getLocalCalendar()
            if (as12AM) {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            return calendar
        }

        @JvmStatic
        fun getDayNameBasedOnDayOfWeek(dayOfWeek: Int): String? {
            return try {
                DAYS[dayOfWeek - 1]
            } catch (e: Error) {
                null
            }
        }

        @JvmStatic
        fun getDayName(timeInMillis: Long): String? {
            return getDayNameBasedOnDayOfWeek(
                getLocalCalendar().apply {
                    this.timeInMillis = timeInMillis
                }.get(Calendar.DAY_OF_WEEK)
            )
        }

        fun getCalendar(timeInMillis: Long): Calendar {
            return getLocalCalendar().apply {
                this.timeInMillis = timeInMillis
            }
        }

        @JvmStatic
        fun convertCalenderToString(
            calender: Calendar,
            dateFormat: String = DATE_FORMAT
        ): String {
            val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
            return simpleDateFormat.format(calender.time)
        }

        @JvmStatic
        fun convertCalenderToString(
            dateInMillis: Long,
            dateFormat: String = DATE_FORMAT
        ): String {
            val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
            return simpleDateFormat.format(dateInMillis)
        }

        @JvmStatic
        fun convertStringToCalender(
            dateString: String?,
            dateFormat: String
        ): Calendar? {
            if (dateString == null) {
                return null
            }
            val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
            val date = simpleDateFormat.parse(dateString)
            if (date != null) {
                val calender = getLocalCalendar()
                calender.time = date
                return calender
            }
            return null
        }

        @JvmStatic
        fun convertDateStringToSpecifiedDateString(
            dateString: String?,
            dateFormat: String = DATE_FORMAT,
            requiredDateFormat: String = DATE_FORMAT_DISPLAY
        ): String? {
            if (dateString == null) {
                return null
            }
            val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
            val date = simpleDateFormat.parse(dateString)
            if (date != null) {
                val reqSimpleDateFormat =
                    SimpleDateFormat(requiredDateFormat, Locale.getDefault())
                return reqSimpleDateFormat.format(date)
            }
            return null
        }

        @JvmStatic
        fun isYesterday(
            dateString: String?,
            dateFormat: String = DATE_FORMAT
        ): Boolean {
            if (dateString == null) {
                return false
            }
            val cal: Calendar = getLocalCalendar()
            cal.roll(Calendar.DATE, false)
            return convertCalenderToString(cal, dateFormat) == dateString
        }

        @JvmStatic
        fun isToday(dateString: String?, dateFormat: String = DATE_FORMAT): Boolean {
            if (dateString == null) {
                return false
            }
            val cal: Calendar = getLocalCalendar()
            return convertCalenderToString(cal, dateFormat) == dateString
        }


        @JvmStatic
        fun getFancyDay(dateString: String?, dateFormat: String = DATE_FORMAT): String {
            return when {
                isToday(dateString, dateFormat) -> {
                    "Today"
                }

                isYesterday(dateString, dateFormat) -> {
                    "Yesterday"
                }

                else -> {
                    convertDateStringToSpecifiedDateString(
                        dateString,
                        dateFormat,
                        DATE_FORMAT_DISPLAY
                    ) ?: dateString!!
                }
            }
        }

        @JvmStatic
        fun createCalendarForHourOfDayAndMin(
            hourOfDay: Int,
            minute: Int = 0
        ): Calendar {
            val calender = getLocalCalendar()
            calender.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calender.set(Calendar.MINUTE, minute)
            calender.set(Calendar.SECOND, 0)
            return calender
        }

        /**
         * @return 01,02,03....31
         */
        @JvmStatic
        fun getDayFromDateString(date: String?, format: String): String? {
            if (date == null) {
                return null
            }
            val calendar = convertStringToCalender(date, format)
            if (calendar != null) {
                return SimpleDateFormat("dd", Locale.getDefault()).format(calendar.time)
            }
            return date.substring(0, 2)
        }


        @JvmStatic
        fun getMonthFromDateString(date: String, format: String): String {
            val calendar = convertStringToCalender(date, format)
            if (calendar != null) {
                return SimpleDateFormat(
                    "MMM",
                    Locale.getDefault()
                ).format(calendar.time)
            }
            return date.substring(2, 5)
        }


        fun subtractDaysFromCalendar(
            dateString: String?,
            howManyDays: Int,
            dateFormat: String = DATE_FORMAT
        ): String {
            val calendar: Calendar = if (dateString != null) {
                convertStringToCalender(
                    dateString,
                    DATE_FORMAT
                )!!
            } else {
                getLocalCalendar()
            }
            calendar.add(Calendar.DATE, -abs(howManyDays))
            return convertCalenderToString(
                calendar,
                DATE_FORMAT
            )
        }


    }
}