package com.diegobezerra.core.cinemais.data.cinemas

import com.diegobezerra.core.cinemais.domain.model.Location
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.cinemais.domain.model.Tickets
import com.diegobezerra.core.result.Result

interface CinemasDataSource {

    /**
     * Returns the schedule of the cinema with the given ID
     * @param id Cinema ID
     */
    suspend fun getSchedule(id: Int): Result<Schedule>

    /**
     * Returns the tickets of the cinema with the given ID
     * @param id Cinema ID
     */
    suspend fun getTickets(id: Int): Result<Tickets>

    /**
     * Returns the location of the cinema with the given ID
     * @param id Cinema ID
     */
    suspend fun getLocation(id: Int): Result<Location>

}