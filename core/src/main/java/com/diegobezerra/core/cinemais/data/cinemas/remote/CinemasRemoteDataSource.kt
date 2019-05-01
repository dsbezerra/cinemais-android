package com.diegobezerra.core.cinemais.data.cinemas.remote

import com.diegobezerra.core.cinemais.data.CinemaisService
import com.diegobezerra.core.cinemais.data.cinemas.CinemasDataSource
import javax.inject.Inject

class CinemasRemoteDataSource @Inject constructor(
    private val service: CinemaisService
) : CinemasDataSource {

    override fun getLocation(id: Int) = service.cinemaLocation(id)

    override fun getSchedule(id: Int) = service.cinemaSchedule(id)

    override fun getTickets(id: Int) = service.cinemaTickets(id)

}