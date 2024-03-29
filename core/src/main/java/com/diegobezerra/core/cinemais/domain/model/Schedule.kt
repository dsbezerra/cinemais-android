package com.diegobezerra.core.cinemais.domain.model

import com.diegobezerra.core.util.DateUtils
import com.diegobezerra.core.util.DateUtils.Companion.BRAZIL
import java.text.SimpleDateFormat
import java.util.Date

typealias Disclaimer = Map<Char, DisclaimerEntry>

data class Schedule(
    val cinema: Cinema,
    val disclaimer: Disclaimer,
    val week: DateRange,
    var sessions: List<Session>
) {

    var days: List<ScheduleDay> = emptyList()

    // Prevent unnecessary days map recreation
    private var generatedTimestamp: Long = 0L

    init {
        createDays()
    }

    fun recreateDays(matcher: SessionMatcher? = null): Schedule {
        createDays(matcher)
        return this
    }

    private fun createDays(matcher: SessionMatcher? = null) {
        val today = DateUtils.calendarAtStartOfDay(null).timeInMillis
        if (week.end.time < today) {
            // Better display nothing than a wrong schedule.
            return
        }

        val map = linkedMapOf<String, ScheduleDay>()
        if (sessions.isNotEmpty()) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", BRAZIL)
            for (session in sessions) {
                // Ignore all sessions before the current day
                if (session.startTimeDate == null || session.startTimeDate?.time!! < today) {
                    continue
                }

                if (matcher != null && !matcher.matches(session)) {
                    continue
                }

                try {
                    val key = dateFormat.format(session.startTimeDate!!)
                    if (key != "") {
                        if (!map.containsKey(key)) {
                            map[key] = ScheduleDay(
                                day = DateUtils.calendarAtStartOfDay(session.startTimeDate).time,
                                sessions = mutableListOf(session)
                            )
                        } else {
                            map[key]?.let { it.sessions += session }
                        }
                    }
                } catch (e: Exception) {
                    // No-op
                }
            }
            generatedTimestamp = today
        }
        days = map.toSortedMap().values.toList()
    }

    fun getDay(day: Int): ScheduleDay? {
        return if (day >= 0 && day < days.size) {
            days[day]
        } else {
            null
        }
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
        const val VideoFormatBoth = "2D-3D"

        const val RoomMagicD = "room_magic_d"
        const val RoomVIP = "room_vip"
    }

    fun getProperties(): List<String> {
        return mutableListOf<String>().also { properties ->
            properties += version
            properties += format
            if (magic) properties += RoomMagicD
            if (vip) properties += RoomVIP
        }
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