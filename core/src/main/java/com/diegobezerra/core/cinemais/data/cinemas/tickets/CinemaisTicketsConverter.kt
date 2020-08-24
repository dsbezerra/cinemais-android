package com.diegobezerra.core.cinemais.data.cinemas.tickets

import com.diegobezerra.core.cinemais.data.asJsoup
import com.diegobezerra.core.cinemais.domain.model.MinMaxPeople
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
import java.util.Locale

object CinemaisTicketsConverter : Converter<ResponseBody, Tickets> {

    override fun convert(value: ResponseBody): Tickets {
        return parseTickets(value.asJsoup().body())
    }

    private fun isDriveIn(element: Element): Boolean {
        val headers = element.select("table th")
        if (headers.size == 2) {
            if (headers.first().text().trim() == "Dias da Semana"
                && headers.last().text().trim() == "Sessões"
            ) {
                element.select("table tr").forEach {
                    if (it.text().startsWith("Carros")) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun parseTickets(element: Element): Tickets {
        val result = if (!isDriveIn(element)) {
            parseNormalTickets(element)
        } else {
            parseDriveInTickets(element)
        }
        val buyOnlineUrl = element.select("a[title='Comprar ingressos on-line']").attr("href")
        return Tickets(
            tickets = result,
            buyOnlineUrl = buyOnlineUrl
        )
    }

    private fun parseNormalTickets(element: Element): MutableList<Ticket> {
        val result = mutableListOf<Ticket>()
        element.select("table tr").forEachIndexed { rowIndex, row ->
            if (rowIndex > 1) { // Skip header and 2D or 3D rows
                var weekdays = Weekdays(disclaimer = "")
                row.select("td").forEachIndexed ForEachColumn@{ i, column ->
                    if (i == 0) {
                        weekdays = parseWeekdays(column.text().trim())
                        if (weekdays.isEmpty()) {
                            return@ForEachColumn
                        }
                    } else {

                        val full = parsePrice(column.text().trim())
                        if (full == 0.0f) {
                            return@ForEachColumn
                        }

                        var ticket: Ticket? = null
                        when (i) {
                            // Rooms columns
                            1, 2 -> {
                                ticket = Ticket.Normal(
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
                                ticket = Ticket.Normal(
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
                                ticket = Ticket.Normal(
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
        return result
    }

    private fun parseDriveInTickets(element: Element): MutableList<Ticket> {
        val result = mutableListOf<Ticket>()
        var mapping: HashMap<Int, MinMaxPeople>? = null
        element.select("table tr").forEachIndexed { rowIndex, row ->
            if (rowIndex == 1) {
                mapping = parseDriveInHeader(element)
            } else if (rowIndex > 1) {
                if (mapping.isNullOrEmpty()) {
                    return@forEachIndexed
                }
                var weekdays = Weekdays(disclaimer = "")
                row.select("td").forEachIndexed ForEachColumn@{ columnIndex, column ->
                    if (columnIndex == 0) {
                        weekdays = parseWeekdays(column.text().trim())
                        if (weekdays.isEmpty()) {
                            return@ForEachColumn
                        }
                    } else {
                        val people = mapping?.get(columnIndex - 1)
                        val price = parsePrice(column.text().trim())
                        if (price == 0.0f || people == null) {
                            return@ForEachColumn
                        }
                        result.add(
                            Ticket.DriveIn(
                                weekdays = weekdays,
                                price = price,
                                people = people,
                            )
                        )
                    }
                }
            }
        }
        return result
    }

    private fun parseDriveInHeader(element: Element): HashMap<Int, MinMaxPeople> {
        val result = hashMapOf<Int, MinMaxPeople>()
        element.select("td").forEachIndexed { columnIndex, column ->
            val minMaxPeople = parseMinMaxPeople(column.text())
            if (minMaxPeople != null) {
                result[columnIndex] = minMaxPeople
            }
        }
        return result
    }

    private fun parseMinMaxPeople(text: String): MinMaxPeople? {
        var result: MinMaxPeople? = null

        var w = text
        while (true) {
            if (w.isEmpty()) {
                break
            }

            val pair = w.breakBySpaces()
            if (pair.first == "até") {
                val min = pair.second.breakBySpaces()
                if (min.first.isDigitsOnly()) {
                    val value = min.first.toInt()
                    result = MinMaxPeople(value, value)
                    break
                }
            } else if (pair.first.isDigitsOnly()) {
                val min = pair.first.toInt()
                val next = pair.second.breakBySpaces()
                if (next.first == "à") {
                    val max = next.second.breakBySpaces()
                    if (max.first.isDigitsOnly()) {
                        val value = max.first.toInt()
                        result = MinMaxPeople(min, value)
                        break
                    }
                }
            }

            w = pair.second
        }

        return result
    }

    fun parseWeekdays(text: String): Weekdays {
        val result = mutableListOf<Weekday>()

        var disclaimer = ""
        var holidays = false
        var exceptHolidays = false
        var exceptPreviews = false

        var toWasSeen = false

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
                if (!toWasSeen && pair.first == "à") {
                    toWasSeen = true
                }
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
        val lastPrefix = if (toWasSeen && result.size == 2) {
            "à"
        } else {
            "e"
        }
        return Weekdays(
            weekdays = result,
            disclaimer = disclaimer,
            holidays = holidays,
            exceptHolidays = exceptHolidays,
            exceptPreviews = exceptPreviews,
            lastPrefix = lastPrefix
        )
    }

    private fun parseWeekday(text: String): Weekday? {
        val lc = text.toLowerCase(Locale.getDefault())
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

    private fun String.isDigitsOnly(): Boolean {
        forEach {
            if (!it.isDigit()) {
                return false
            }
        }
        return true
    }
}
