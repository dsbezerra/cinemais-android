package com.diegobezerra.core.cinemais.data.cinemas.tickets

import com.diegobezerra.core.cinemais.data.TestUtil
import com.diegobezerra.core.cinemais.domain.model.Weekday
import com.diegobezerra.core.cinemais.domain.model.Weekdays
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Calendar.FRIDAY
import java.util.Calendar.MONDAY
import java.util.Calendar.SATURDAY
import java.util.Calendar.SUNDAY
import java.util.Calendar.THURSDAY
import java.util.Calendar.TUESDAY

class CinemaisTicketsConverterTest {

    @Test
    fun parsesHtml() {
        val data = TestUtil.loadCinemaisHtmlPage("tickets")
        val response = ResponseBody.create(null, data)
        val result = CinemaisTicketsConverter.convert(response)
        assertNotNull(result)
    }

    @Test
    fun parseWeekdays() {
        var expected = Weekdays(
            weekdays = listOf(
                Weekday(MONDAY, "Segunda"),
                Weekday(TUESDAY, "Terça"),
                Weekday(THURSDAY, "Quinta"),
                Weekday(FRIDAY, "Sexta")
            ),
            disclaimer = "(exceto feriados e pré-estreias especiais)",
            holidays = false,
            exceptHolidays = true,
            exceptPreviews = true
        )
        var actual =
            CinemaisTicketsConverter.parseWeekdays("2ª, 3ª, 5ª e 6ª  (exceto feriados e pré-estreias especiais)")
        assertEquals(expected, actual)

        expected = Weekdays(
            weekdays = listOf(
                Weekday(SATURDAY, "Sábado"),
                Weekday(SUNDAY, "Domingo")
            ),
            disclaimer = "",
            holidays = true,
            exceptHolidays = false,
            exceptPreviews = false
        )
        actual = CinemaisTicketsConverter.parseWeekdays("Sábados, domingos e feriados")
        assertEquals(expected, actual)
    }

    @Test
    fun parsePrice() {
        val expected = 18.0f
        val actual = CinemaisTicketsConverter.parsePrice("R$ 18,00 (Inteira)")
        assertEquals(expected, actual)
    }

}