package com.diegobezerra.core.util

import com.diegobezerra.core.cinemais.domain.model.DateRange
import java.util.Calendar
import java.util.Calendar.DAY_OF_WEEK
import java.util.Calendar.HOUR
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MILLISECOND
import java.util.Calendar.MINUTE
import java.util.Calendar.SECOND
import java.util.Calendar.THURSDAY
import java.util.Calendar.WEDNESDAY
import java.util.Calendar.WEEK_OF_MONTH
import java.util.Calendar.getInstance
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale

class DateUtils {

    companion object {

        val BRAZIL = Locale("pt", "BR")

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
    }
}
