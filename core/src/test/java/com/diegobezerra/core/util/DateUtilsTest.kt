package com.diegobezerra.core.util

import com.diegobezerra.core.cinemais.domain.model.DateRange
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.GregorianCalendar

class DateUtilsTest {

    @Test
    fun playingRange_isCorrect() {
        // Date 18/03/2019 should return range
        // start:  14/03/2019 - Thursday
        // end:    20/03/2019 - Wednesday
        var expectedRange = DateRange(
            start = GregorianCalendar(2019, 2, 14).time,
            end = GregorianCalendar(2019, 2, 20).time
        )
        var actualRange = DateUtils.playingRange(GregorianCalendar(2019, 2, 18).time)
        assertEquals(expectedRange, actualRange)

        // Date 30/12/2018 should return range
        // start:  27/12/2018 - Thursday
        // end:    02/01/2019 - Wednesday
        expectedRange = DateRange(
            start = GregorianCalendar(2018, 11, 27).time,
            end = GregorianCalendar(2019, 0, 2).time
        )
        actualRange = DateUtils.playingRange(GregorianCalendar(2018, 11, 30).time)
        assertEquals(expectedRange, actualRange)
    }
}