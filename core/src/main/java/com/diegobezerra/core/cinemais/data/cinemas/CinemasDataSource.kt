package com.diegobezerra.core.cinemais.data.cinemas

import com.diegobezerra.core.cinemais.domain.model.Location
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.cinemais.domain.model.Tickets
import io.reactivex.Single

interface CinemasDataSource {

    fun getLocation(id: Int): Single<Location>

    fun getSchedule(id: Int): Single<Schedule>

    fun getTickets(id: Int): Single<Tickets>

}