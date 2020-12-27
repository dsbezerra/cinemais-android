package com.diegobezerra.core.cinemais.domain.model

import org.junit.Assert
import org.junit.Test
import java.util.Date
import java.util.GregorianCalendar

/**
 * Unit tests for [Schedule]
 */
class ScheduleTest {

    @Test
    fun dates_isCorrect() {
        val range = DateRange(
            start = GregorianCalendar(2019, 2, 14).time,
            end = GregorianCalendar(2019, 3, 20).time
        )
        range.dates().forEachIndexed { index, it ->
            val copy = (range.start.clone() as Date).apply {
                date += index
            }
            Assert.assertTrue(copy.date == it.date)
        }
    }
}