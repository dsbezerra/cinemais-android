package com.diegobezerra.core.util

import android.text.format.DateUtils
import com.diegobezerra.core.cinemais.domain.model.DateRange
import java.util.Calendar
import java.util.Calendar.DATE
import java.util.Calendar.DAY_OF_WEEK
import java.util.Calendar.HOUR
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MILLISECOND
import java.util.Calendar.MINUTE
import java.util.Calendar.MONTH
import java.util.Calendar.SECOND
import java.util.Calendar.THURSDAY
import java.util.Calendar.WEDNESDAY
import java.util.Calendar.WEEK_OF_MONTH
import java.util.Calendar.YEAR
import java.util.Calendar.getInstance
import java.util.Date
import java.util.Locale

class DateUtils {

    companion object {

        val BRAZIL = Locale("pt", "BR")

        fun dateAsString(date: Calendar = getInstance()): String {
            return "${date[YEAR]}-${date[MONTH]}-${date[DATE]}"
        }

        fun playingRange(date: Date? = null): DateRange {
            val start = calendarAtStartOfDay(date)
            val end = calendarAtStartOfDay(date)
            val init = calendarAtStartOfDay(date)
            if (init.get(DAY_OF_WEEK) < THURSDAY) {
                start.run {
                    add(WEEK_OF_MONTH, -1)
                    set(DAY_OF_WEEK, THURSDAY)
                }
                end.set(DAY_OF_WEEK, WEDNESDAY)
            } else {
                start.set(DAY_OF_WEEK, THURSDAY)
                end.run {
                    add(WEEK_OF_MONTH, 1)
                    set(DAY_OF_WEEK, WEDNESDAY)
                }
            }
            return DateRange(
                start = start.time,
                end = end.time
            )
        }

        fun calendarAtStartOfDay(date: Date? = null): Calendar {
            return getInstance().apply {
                date?.let { time = date }
                set(HOUR, 0)
                set(HOUR_OF_DAY, 0)
                set(MINUTE, 0)
                set(SECOND, 0)
                set(MILLISECOND, 0)
            }
        }

        fun isToday(`when`: Long) = DateUtils.isToday(`when`)

        fun isTomorrow(`when`: Long): Boolean {
            val whenCalendar = getInstance().apply {
                timeInMillis = `when`
            }
            val tomorrow = getInstance().apply {
                add(DATE, 1)
            }
            return isSameDay(whenCalendar, tomorrow)
        }

        // Returns true only if b Calendar corresponds to tomorrow of a Calendar
        fun isTomorrow(a: Calendar, b: Calendar): Boolean {
            val cloned = getInstance().apply {
                timeInMillis = a.timeInMillis
                add(DATE, 1)
            }
            return isSameDay(cloned, b)
        }

        private fun isSameDay(a: Calendar, b: Calendar): Boolean {
            return a.get(DATE) == b.get(DATE) &&
                a.get(MONTH) == b.get(MONTH) &&
                a.get(YEAR) == b.get(YEAR)
        }
    }
}
