package com.diegobezerra.core.cinemais.data.cinemas.remote

import com.diegobezerra.core.cinemais.data.CinemaisService
import com.diegobezerra.core.cinemais.data.cinemas.CinemasDataSource
import com.diegobezerra.core.util.safeRequest
import javax.inject.Inject

class CinemasRemoteDataSource @Inject constructor(
    private val service: CinemaisService
) : CinemasDataSource {

    override suspend fun getSchedule(id: Int, date: String?) = safeRequest(
        call = {
            if (date == null) {
                service.schedule(id)
            } else {
                service.nextWeekSchedule(id, date)
            }
        }
    )

    override suspend fun getTickets(id: Int) = safeRequest(
        call = { service.tickets(id) }
    )

    override suspend fun getLocation(id: Int) = safeRequest(
        call = { service.location(id) }
    )

}