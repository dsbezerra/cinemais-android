package com.diegobezerra.core.cinemais.data.cinemas

import com.diegobezerra.core.cinemais.data.cinemas.remote.CinemasRemoteDataSource
import com.diegobezerra.core.cinemais.data.home.HomeRepository
import com.diegobezerra.core.cinemais.domain.model.Cinemas
import com.diegobezerra.core.cinemais.domain.model.Location
import com.diegobezerra.core.cinemais.domain.model.Schedule
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

    /**
     * Retrieves cinemas list from Home repository.
     */
    fun getCinemas(): Single<Cinemas> {
        return homeRepository.getHome()
            .map {
                it.cinemas
            }
    }

    /**
     * Retrieves schedule for a given cinema ID.
     *
     * @param id ID of the desired cinema.
     */
    fun getSchedule(id: Int): Single<Schedule> {
        val cached = cachedSchedules[id]
        return if (cached != null) {
            Single.just(cached)
                // NOTE(diego): We regenerate days here because these cached schedules may contain
                // old sessions
                .map(Schedule::recreateDays)
                .doOnSuccess {
                    cachedSchedules[id] = it
                }
        } else {
            remoteDataSource.getSchedule(id)
                .doOnSuccess {
                    cachedSchedules[id] = it
                }
        }
    }

    /**
     * Retrieves location data for a given cinema.
     *
     * @param id ID of the desired cinema.
     */
    private fun getLocation(id: Int): Single<Location> {
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

    /**
     * Retrieves schedule data (including location) for a given cinema.
     *
     * @param cinemaId ID of the desired cinema.
     */
    fun getScheduleWithLocation(cinemaId: Int): Single<Schedule> {
        return getSchedule(cinemaId).flatMap { schedule ->
            getLocation(cinemaId).map {
                schedule.cinema.location = it
                schedule
            }
        }
    }

    /**
     * Retrieves tickets data for a given cinema.
     *
     * @param cinemaId ID of the desired cinema.
     */
    fun getTickets(cinemaId: Int): Single<Tickets> {
        val cached = cachedTickets[cinemaId]
        return if (cached != null) {
            Single.just(cached)
        } else {
            remoteDataSource.getTickets(cinemaId)
                .doOnSuccess {
                    cachedTickets[cinemaId] = it
                }
        }
    }

    fun clearSchedule(cinemaId: Int) {
        cachedSchedules.remove(cinemaId)
    }
}