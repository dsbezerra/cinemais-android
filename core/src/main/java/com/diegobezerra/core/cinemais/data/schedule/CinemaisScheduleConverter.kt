package com.diegobezerra.core.cinemais.data.schedule

import com.diegobezerra.core.cinemais.data.asJsoup
import com.diegobezerra.core.cinemais.domain.model.Cinema
import com.diegobezerra.core.cinemais.domain.model.DateRange
import com.diegobezerra.core.cinemais.domain.model.Disclaimer
import com.diegobezerra.core.cinemais.domain.model.DisclaimerEntry
import com.diegobezerra.core.cinemais.domain.model.DisclaimerEntry.Type.EXCEPT
import com.diegobezerra.core.cinemais.domain.model.DisclaimerEntry.Type.ONLY
import com.diegobezerra.core.cinemais.domain.model.Movie
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.cinemais.domain.model.Session
import com.diegobezerra.core.cinemais.domain.model.Session.Companion.VersionDubbed
import com.diegobezerra.core.cinemais.domain.model.Session.Companion.VersionNational
import com.diegobezerra.core.cinemais.domain.model.Session.Companion.VersionSubtitled
import com.diegobezerra.core.cinemais.domain.model.Session.Companion.VideoFormat2D
import com.diegobezerra.core.cinemais.domain.model.Session.Companion.VideoFormat3D
import com.diegobezerra.core.util.DateUtils
import com.diegobezerra.core.util.DateUtils.Companion.BRAZIL
import okhttp3.ResponseBody
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements
import retrofit2.Converter
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

object CinemaisScheduleConverter : Converter<ResponseBody, Schedule> {

    private val DATE_FORMAT by lazy { SimpleDateFormat("dd/MM", BRAZIL) }
    private val DATE_FORMAT_WITH_YEAR by lazy { SimpleDateFormat("dd/MM/yyyy", BRAZIL) }

    override fun convert(value: ResponseBody): Schedule {
        return parseSchedule(
            value.asJsoup().select("#programacaoContainer > div.tableContainer div.spotTabela")
        )
    }

    private fun parseDisclaimer(element: Element): Disclaimer {
        val result = hashMapOf<Char, DisclaimerEntry>()
        val childNodes = element.childNodes()
        val dayRegex by lazy { "\\((\\d{2}/\\d{2})\\)".toRegex() }
        childNodes.forEachIndexed { index, it ->
            if (it is Element && it.`is`("strong")) {
                // Parse entry
                val letter = it.text()
                if (letter.length != 1) {
                    // No-op
                } else {
                    val nextNode = childNodes[index + 1]
                    if (nextNode is TextNode) {
                        val content = nextNode.text().trimStart(' ', '-') // Remove " - "
                        if (content.isNotEmpty()) {
                            val days = mutableListOf<Date>().also {
                                dayRegex.findAll(content).map { it.groupValues[1] }.distinct()
                                    .forEach { s ->
                                        it.add(DATE_FORMAT.parse(s)) // We don't care about the year
                                    }
                            }
                            // This is unexpected. No-op for now
                            if (days.size == 0) {
                            } else {
                                val key = letter[0]
                                result[key] = DisclaimerEntry(
                                    letter = key,
                                    content = content,
                                    type = if (content.contains("exceto", true)) {
                                        EXCEPT
                                    } else {
                                        ONLY
                                    },
                                    days = days
                                )
                            }
                        }
                    }
                }
            }
        }
        return result
    }

    /**
     * Parses and builds session list.
     */
    private fun parseSessions(
        element: Element,
        cinema: Cinema?,
        disclaimer: Disclaimer
    ): List<Session> {
        val result = mutableListOf<Session>()
        val elements = element.select("table tr")
        val dates = DateUtils.playingRange().dates()
        val cinemaId = cinema?.id ?: 0
        elements.forEachIndexed { index, it ->
            if (index > 0) { // Skip header
                val columns = it.select("td")
                if (columns.size != 4) {
                    // No-op
                } else {
                    val room = columns[0].text().toInt()
                    val movieBlock = columns[1]
                    val movieId = Movie.getIdFromUrl(movieBlock.select("a").attr("href"))
                    val movieTitle = movieBlock.text()
                    val movieRating = parseRating(columns[2].select("img").first())

                    // Getting cinema id from print schedule button

                    val is3D = movieBlock.select("img[title='Em 3D']").size == 1
                    val isMagicD = movieBlock.select("img[title='Magic D']").size == 1
                    val isVIP = movieBlock.select("img[title='Vip']").size == 1
                    val format = if (is3D) VideoFormat3D else VideoFormat2D

                    var version = ""
                    val sessionsBlock = columns[3].text()
                    var startingTimesBlock: String? = null
                    val parts = sessionsBlock.split(" - ")
                    when {
                        parts.size == 2 -> { // Dubbed or subtitled movie
                            startingTimesBlock = parts[1]
                            when (parts[0].trim()) {
                                "Dub." -> version = VersionDubbed
                                "Leg." -> version = VersionSubtitled
                            }
                        }
                        parts.size == 1 -> { // National
                            version = VersionNational
                            startingTimesBlock = parts[0]
                        }
                        else -> {
                            // TODO: Diagnostic
                        }
                    }
                    startingTimesBlock?.let { block ->
                        block.split(",").forEach {
                            // NOTE: This may be empty as observed in this case
                            // Leg. - , 00h01A
                            if (it.isNotEmpty()) {
                                val time = it.replace("h", ":").trim()
                                val session = Session(
                                    movieId = movieId,
                                    cinemaId = cinemaId,
                                    movieTitle = movieTitle,
                                    movieRating = movieRating,
                                    room = room,
                                    startTime = time,
                                    version = version,
                                    format = format,
                                    magic = isMagicD,
                                    vip = isVIP
                                )
                                val lastChar = it.last()
                                if (lastChar.isLetter()) {
                                    if (disclaimer.containsKey(lastChar)) {
                                        val entry = disclaimer[lastChar]
                                            ?: throw IllegalStateException("unexpected state")
                                        // Create sessions accordingly with disclaimer
                                        val onlyTime = time.substring(0, time.lastIndex)
                                        if (entry.type == ONLY) {
                                            // NOTE: We wrap around dates here just to get correct year
                                            // since entry dates don't contain year
                                            dates.forEach { date ->
                                                entry.days.forEach { day ->
                                                    if (day.date == date.date && day.month == date.month) {
                                                        result.add(session.copy().apply {
                                                            startTime = onlyTime
                                                            startTimeDate = withTime(date, onlyTime)
                                                        })
                                                    }
                                                }
                                            }
                                        } else if (entry.type == EXCEPT) {
                                            dates.forEach { date ->
                                                var add = true
                                                for (day in entry.days) {
                                                    if (day.date == date.date && day.month == date.month) {
                                                        add = false
                                                        break
                                                    }
                                                }
                                                if (add) {
                                                    result.add(session.copy().apply {
                                                        startTime = onlyTime
                                                        startTimeDate = withTime(date, onlyTime)
                                                    })
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    dates.forEach { date ->
                                        result.add(session.copy().apply {
                                            startTime = time
                                            startTimeDate = withTime(date, time)
                                        })
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
        // NOTE(diego): Make sure we don't have any duplicates!
        return result.distinctBy { it.hashCode() }
    }


    /**
     * Parses cinema information inside tab "Informações"
     */
    private fun parseCinemaInformation(element: Element): List<String> {
        val result = mutableListOf<String>()
        element.select("ul li").forEach {
            result.add(it.text().trim())
        }
        return result
    }

    /**
     * Parses cinema information in page:
     * http://www.cinemais.com.br/programacao/cinema.php?cc=XX
     *
     * scheduleBlock is used to retrieve cinema id, name and location
     * element is used to retrieve data inside tab "Informações"
     */
    private fun parseCinema(scheduleBlock: Element, element: Element): Cinema {
        val cinemaBlock = scheduleBlock.select("div.InfosProgramacao").first()
        val name = cinemaBlock.select("h3").first().text().replace("Cinemais", "").trim()
        val location = cinemaBlock.select("p").first().text().split("-")
        val cityName = if (location.size == 2) location[0] else ""
        val fu = if (location.size == 2) location[1] else ""

        val href = scheduleBlock
            .select("a[title='Imprimir Programação']")
            .first()
            ?.attr("href") ?: ""
        val id = Cinema.idFromUrl(href) ?: 0
        val information = parseCinemaInformation(element)
        return Cinema(
            id = id,
            name = name,
            cityName = cityName,
            fu = fu,
            information = information
        )
    }

    /**
     * Retrieves complete schedule and any cinema data we can find in page:
     * http://www.cinemais.com.br/programacao/cinema.php?cc=XX
     */
    private fun parseSchedule(elements: Elements): Schedule {
        val scheduleBlock = elements.first()
        val week = parseWeek(scheduleBlock.select("small").first())
        val cinema = parseCinema(scheduleBlock, elements[3])
        val disclaimer = parseDisclaimer(scheduleBlock.select("div.disclaimer").first())
        val sessions = parseSessions(scheduleBlock, cinema, disclaimer)
        return Schedule(
            cinema = cinema,
            week = week,
            sessions = sessions,
            disclaimer = disclaimer
        )
    }

    private fun parseWeek(select: Element): DateRange {
        val text = select.text().trim()
        val parts = text.split(" a ")
        val weekRange = DateUtils.playingRange(null)
        return when (parts.size) {
            2 -> {
                try {
                    val start = DATE_FORMAT.parse(parts[0])
                    val end = DATE_FORMAT_WITH_YEAR.parse(parts[1])
                    // NOTE: It was observed that the year of end date is incorrect
                    // when the range is within New Year's Eve.
                    //
                    // Year: 2018
                    // Observed text: 27/12 a 02/01/2018, it should be: 27/12 a 02/01/2019
                    if (start.month == Calendar.DECEMBER && end.month == Calendar.JANUARY) {
                        // Use generated range above which uses current device time.
                        // Not safe but *THIS* is a temporary solution until we have a web service.
                        weekRange
                    } else {
                        // Otherwise, we use the end date, since is the only one with year information.
                        val endCalendar = DateUtils.calendarAtStartOfDay(end).apply {
                            // playingRange functions calculates range relative to the given date
                            // weekdays.
                            //
                            // End date should be a Wednesday.
                            if (get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
                                add(Calendar.DATE, -1)
                            }
                        }
                        DateUtils.playingRange(endCalendar.time)
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                    weekRange
                }
            }
            else -> weekRange
        }
    }

    private fun parseRating(element: Element): Int {
        val src = element.attr("src")
        return when {
            src.contains("LIV") -> -1
            src.contains("10A") -> 10
            src.contains("12A") -> 12
            src.contains("14A") -> 14
            src.contains("16A") -> 16
            src.contains("18A") -> 18
            else -> 0
        }
    }

    private fun withTime(date: Date, time: String): Date {
        return (date.clone() as Date).apply {
            val timeParts = time.split(":")
            hours = timeParts[0].toInt()
            minutes = timeParts[1].toInt()
        }
    }

}