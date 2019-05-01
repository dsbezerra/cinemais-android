package com.diegobezerra.core.cinemais.domain.model

import com.diegobezerra.core.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

typealias Disclaimer = Map<Char, DisclaimerEntry>

data class Schedule(
    val cinema: Cinema,
    val disclaimer: Disclaimer,
    var sessions: List<Session>
) {

    var days: List<ScheduleDay> = emptyList()

    init {
        days = genDays()
    }

    private fun genDays(): List<ScheduleDay> {
        val map = linkedMapOf<String, ScheduleDay>()
        if (sessions.isNotEmpty()) {
            val today = DateUtils.calendarAtStartOfDay(null).timeInMillis
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("pt", "BR"))
            for (session in sessions) {
                // Ignore all sessions before the current time
                if (session.startTimeDate == null || session.startTimeDate?.time!! < today) {
                    continue
                }
                try {
                    val key = dateFormat.format(session.startTimeDate)
                    if (key != "") {
                        if (!map.containsKey(key)) {
                            map[key] = ScheduleDay(
                                day = DateUtils.calendarAtStartOfDay(session.startTimeDate).time,
                                sessions = mutableListOf(session)
                            )
                        } else {
                            map[key]?.let { day ->
                                day.sessions += session
                            }
                        }
                    }
                } catch (e: Exception) {
                    // No-op
                }
            }
        }
        return map.toSortedMap().values.toList()
    }
}

data class ScheduleDay(
    val day: Date,
    val sessions: MutableList<Session> = mutableListOf()
)

data class Session(
    val movieId: Int,
    val cinemaId: Int,
    val movieTitle: String,
    val movieRating: Int,
    val room: Int,
    val version: String,
    val format: String,
    val magic: Boolean,
    val vip: Boolean,
    var startTime: String,
    var startTimeDate: Date? = null
) {

    companion object {

        const val VersionNational = "national"
        const val VersionSubtitled = "subtitled"
        const val VersionDubbed = "dubbed"
        const val VideoFormat2D = "2D"
        const val VideoFormat3D = "3D"

    }
}

data class DisclaimerEntry(
    val letter: Char,
    val content: String,
    val type: Type,
    val days: List<Date>
) {
    enum class Type {
        EXCEPT,
        ONLY
    }
}

data class DateRange(
    val start: Date,
    val end: Date
) {

    fun dates(): List<Date> {
        val result = mutableListOf(start)

        var d = start.clone() as Date
        while (d != end) {
            result.add(d.apply {
                date += 1
            })
            d = d.clone() as Date
        }

        return result
    }

}