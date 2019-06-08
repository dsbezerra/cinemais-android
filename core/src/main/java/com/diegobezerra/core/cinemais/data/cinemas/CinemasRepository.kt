package com.diegobezerra.core.cinemais.data.cinemas

import com.diegobezerra.core.cinemais.data.cinemas.remote.CinemasRemoteDataSource
import com.diegobezerra.core.cinemais.data.home.HomeRepository
import com.diegobezerra.core.cinemais.domain.model.Cinemas
import com.diegobezerra.core.cinemais.domain.model.Location
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.cinemais.domain.model.ScheduleDay
import com.diegobezerra.core.cinemais.domain.model.Tickets
import io.reactivex.Single
import javax.inject.Inject

class CinemasRepository @Inject constructor(
    private val homeRepository: HomeRepository,
    private val remoteDataSource: CinemasRemoteDataSource
) {

    private val cachedSchedules: HashMap<Int, Schedule> = hashMapOf()
    private val cachedLocations: HashMap<Int, Location> = hashMapOf()
    private val cachedTickets: HashMap<Int, Tickets> = hashMapOf()

    fun getCinemas(): Single<Cinemas> = homeRepository.getHome().map { it.cinemas }

    fun getSchedule(id: Int): Single<Schedule> {
        val cached = cachedSchedules[id]
        return if (cached != null) {
            Single.just(cached)
                // NOTE(diego): We regenerate days here because these cached schedules may contain
                // old sessions
                .map(Schedule::recreateDays)
        } else {
            remoteDataSource.getSchedule(id)
                .doOnSuccess {
                    cachedSchedules[id] = it
                }
        }
    }

    fun getLocation(id: Int): Single<Location> {
        val cached = cachedLocations[id]
        return if (cached != null) {
            Single.just(cached)
        } else {
            remoteDataSource.getLocation(id)
                .doOnSuccess {
                    cachedLocations[id] = it
                }
        }
    }

    fun getScheduleWithLocation(id: Int): Single<Schedule> {
        return getSchedule(id).flatMap { schedule ->
            getLocation(id).map {
                schedule.cinema.location = it
                schedule
            }
        }
    }

    fun getScheduleDay(id: Int, day: Int): Single<ScheduleDay> {
        return getSchedule(id).map { it.days[day] }
    }

    fun getTickets(id: Int): Single<Tickets> {
        val cached = cachedTickets[id]
        return if (cached != null) {
            Single.just(cached)
        } else {
            remoteDataSource.getTickets(id)
                .doOnSuccess {
                    cachedTickets[id] = it
                }
        }
    }
}