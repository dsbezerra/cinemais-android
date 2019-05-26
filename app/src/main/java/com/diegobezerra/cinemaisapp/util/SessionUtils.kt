package com.diegobezerra.cinemaisapp.util

import com.diegobezerra.cinemaisapp.ui.schedule.SessionGroup
import com.diegobezerra.core.cinemais.domain.model.Session

class SessionUtils {

    companion object {

        fun group(sessions: List<Session>): List<SessionGroup> {
            val result = mutableListOf<SessionGroup>()
            val groups = sessions.groupBy {
                var key = "${it.movieTitle}.${it.format}.${it.version}"
                if (it.magic) {
                    key += ".magic"
                }
                if (it.vip) {
                    key += ".vip"
                }
                key
            }.toSortedMap()

            // Now generate the list
            groups.keys.forEach { groupKey ->
                val localSessions = groups[groupKey]
                localSessions?.let {
                    if (it.isNotEmpty()) {
                        var group: SessionGroup? = null
                        it.forEach { session ->
                            if (group == null) {
                                group = SessionGroup(
                                    id = groupKey,
                                    movieId = session.movieId,
                                    movieTitle = session.movieTitle,
                                    movieRating = session.movieRating,
                                    format = session.format,
                                    version = session.version,
                                    magic = session.magic,
                                    vip = session.vip
                                )
                            }
                            group!!.add(session)
                        }
                        group?.let { sg ->
                            // We don't care about room number and because of this times may
                            // be unsorted
                            sg.sessions.sortBy { s ->
                                s.startTime
                            }
                            result.add(sg)
                        }
                    }
                }
            }

            return result
        }
    }
}

fun List<Session>.groups(): List<SessionGroup> {
    return SessionUtils.group(this)
}