package com.diegobezerra.core.cinemais.data.cinemas.tickets

import com.diegobezerra.core.cinemais.data.asJsoup
import com.diegobezerra.core.cinemais.domain.model.Session.Companion.VideoFormat2D
import com.diegobezerra.core.cinemais.domain.model.Session.Companion.VideoFormat3D
import com.diegobezerra.core.cinemais.domain.model.Session.Companion.VideoFormatBoth
import com.diegobezerra.core.cinemais.domain.model.Ticket
import com.diegobezerra.core.cinemais.domain.model.Tickets
import com.diegobezerra.core.cinemais.domain.model.Weekday
import com.diegobezerra.core.cinemais.domain.model.Weekdays
import com.diegobezerra.core.util.breakBySpaces
import okhttp3.ResponseBody
import org.jsoup.nodes.Element
import retrofit2.Converter
import java.util.Calendar.FRIDAY
import java.util.Calendar.MONDAY
import java.util.Calendar.SATURDAY
import java.util.Calendar.SUNDAY
import java.util.Calendar.THURSDAY
import java.util.Calendar.TUESDAY
import java.util.Calendar.WEDNESDAY

object CinemaisTicketsConverter : Converter<ResponseBody, Tickets> {

    override fun convert(value: ResponseBody): Tickets {
        return parseTickets(value.asJsoup().body())
    }

    private fun parseTickets(element: Element): Tickets {
        val result = mutableListOf<Ticket>()
        element.select("table tr").forEachIndexed { rowIndex, row ->
            if (rowIndex > 1) { // Skip header and 2D or 3D rows
                var weekdays = Weekdays(disclaimer = "")
                row.select("td").forEachIndexed { i, column ->
                    if (i == 0) {
                        weekdays = parseWeekdays(column.text().trim())
                        if (weekdays.isEmpty()) {
                            return@forEachIndexed
                        }
                    } else {

                        val full = parsePrice(column.text().trim())
                        if (full == 0.0f) {
                            return@forEachIndexed
                        }

                        var ticket: Ticket? = null
                        when (i) {
                            // Rooms columns
                            1, 2 -> {
                                ticket = Ticket(
                                    weekdays,
                                    full,
                                    half = full / 2,
                                    format = if (i == 1) VideoFormat2D else VideoFormat3D,
                                    vip = false,
                                    magic = false
                                )
                            }
                            // Magic D columns
                            3, 4 -> {
                                ticket = Ticket(
                                    weekdays,
                                    full,
                                    half = full / 2,
                                    format = if (i == 3) VideoFormat2D else VideoFormat3D,
                                    magic = true,
                                    vip = false
                                )
                            }
                            // Magic D VIP columns
                            5 -> {
                                ticket = Ticket(
                                    weekdays,
                                    full,
                                    half = full / 2,
                                    format = VideoFormatBoth,
                                    magic = true,
                                    vip = true
                                )
                            }
                        }
                        ticket?.let { result.add(it) }
                    }
                }
            }
        }

        val buyOnlineUrl = element.select("a[title='Comprar ingressos on-line']").attr("href")
        return Tickets(
            tickets = result,
            buyOnlineUrl = buyOnlineUrl
        )
    }

    fun parseWeekdays(text: String): Weekdays {
        val result = mutableListOf<Weekday>()

        var disclaimer = ""
        var holidays = false
        var exceptHolidays = false
        var exceptPreviews = false

        var w = text
        while (true) {

            if (w.isEmpty()) {
                break
            }

            val pair = w.breakBySpaces()
            val weekday = parseWeekday(pair.first)
            if (weekday != null) {
                if (!result.contains(weekday)) {
                    result.add(weekday)
                }
            } else {
                if (pair.first.isNotEmpty() && pair.first[0] == '(') {
                    if (pair.first.drop(1).startsWith("exceto")) {
                        exceptHolidays = pair.second.contains("feriados")
                        exceptPreviews = pair.second.contains("pré-estreias")
                        disclaimer = "${pair.first} ${pair.second}"
                        break
                    }
                } else if (pair.first.contains("feriados")) {
                    holidays = true
                    break
                }
            }

            w = pair.second
        }

        return Weekdays(
            weekdays = result,
            disclaimer = disclaimer,
            holidays = holidays,
            exceptHolidays = exceptHolidays,
            exceptPreviews = exceptPreviews
        )
    }

    private fun parseWeekday(text: String): Weekday? {
        val lc = text.toLowerCase()
        if (lc.startsWith("domingo") ||
            lc.startsWith("dom.") ||
            lc.startsWith("dom")
        ) {
            return Weekday(SUNDAY, "Domingo")
        } else if (lc.startsWith("2ª") ||
            lc.startsWith("segunda") ||
            lc.startsWith("seg.") ||
            lc.startsWith("seg")
        ) {
            return Weekday(MONDAY, "Segunda")
        } else if (lc.startsWith("3ª") ||
            lc.startsWith("terça") ||
            lc.startsWith("ter.") ||
            lc.startsWith("ter")
        ) {
            return Weekday(TUESDAY, "Terça")
        } else if (lc.startsWith("4ª") ||
            lc.startsWith("quarta") ||
            lc.startsWith("qua.") ||
            lc.startsWith("qua")
        ) {
            return Weekday(WEDNESDAY, "Quarta")
        } else if (lc.startsWith("5ª") ||
            lc.startsWith("quinta") ||
            lc.startsWith("qui.") ||
            lc.startsWith("qui")
        ) {
            return Weekday(THURSDAY, "Quinta")
        } else if (lc.startsWith("6ª") ||
            lc.startsWith("sexta") ||
            lc.startsWith("sex.") ||
            lc.startsWith("sex")
        ) {
            return Weekday(FRIDAY, "Sexta")
        } else if (lc.startsWith("sábado") ||
            lc.startsWith("sáb.") ||
            lc.startsWith("sáb")
        ) {
            return Weekday(SATURDAY, "Sábado")
        }
        return null
    }

    fun parsePrice(text: String): Float {
        var result = 0.0f
        if (!text.startsWith("R$ ")) {
            return result
        }
        try {
            result = text.replace(",", ".").substring(3, 8).toFloat()
        } catch (e: NumberFormatException) {
            // No-op
        }
        return result
    }

}