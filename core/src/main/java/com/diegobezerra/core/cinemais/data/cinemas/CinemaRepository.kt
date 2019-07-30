package com.diegobezerra.core.cinemais.data.cinemas

import com.diegobezerra.core.cinemais.data.cinemas.remote.CinemasRemoteDataSource
import com.diegobezerra.core.cinemais.data.home.HomeRepository
import com.diegobezerra.core.cinemais.domain.model.Cinemas
import com.diegobezerra.core.cinemais.domain.model.Location
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.cinemais.domain.model.SessionMatcher
import com.diegobezerra.core.cinemais.domain.model.Tickets
import com.diegobezerra.core.result.Result
import com.diegobezerra.core.result.Result.Success
import com.diegobezerra.core.result.getRemoteAndCache
import javax.inject.Inject

class CinemaRepository @Inject constructor(
    private val homeRepository: HomeRepository,
    private val remoteDataSource: CinemasRemoteDataSource
) {

    private val scheduleCache: HashMap<Int, Schedule> = hashMapOf()
    private val locationCache: HashMap<Int, Location> = hashMapOf()
    private val ticketsCache: HashMap<Int, Tickets> = hashMapOf()

    /**
     * Retrieves cinemas list from Home repository.
     */
    suspend fun getCinemas(): Result<Cinemas> {
        val result = homeRepository.getHome()
        if (result is Success) {
            return Success(result.data.cinemas)
        }
        return result as Result.Error
    }

    /**
     * Returns the schedule of the cinema with the given ID
     * @param id Cinema ID
     */
    suspend fun getSchedule(id: Int, matcher: SessionMatcher? = null): Result<Schedule> {
        val cached = scheduleCache[id]
        return if (cached != null) {
            // NOTE(diego): We recreate days here because these cached schedules may contain
            // old sessions or may need to be re-filtered
            Success(cached.recreateDays(matcher))
        } else {
            getRemoteAndCache(
                call = {
                    remoteDataSource.getSchedule(id).also {
                        if (it is Success && matcher != null) {
                            it.data.recreateDays(matcher)
                        }
                    }
                },
                cacheMap = scheduleCache,
                entryKey = id
            )
        }
    }

    /**
     * Retrieves location data for a given cinema.
     *
     * @param id ID of the desired cinema.
     */
    private suspend fun getLocation(id: Int): Result<Location> {
        val cached = locationCache[id]
        return if (cached != null) {
            Success(cached)
        } else {
            getRemoteAndCache(
                call = { remoteDataSource.getLocation(id) },
                cacheMap = locationCache,
                entryKey = id
            )
        }
    }

    /**
     * Retrieves schedule data (including location) for a given cinema.
     *
     * @param cinemaId ID of the desired cinema.
     */
    suspend fun getScheduleWithLocation(
        cinemaId: Int,
        matcher: SessionMatcher? = null
    ): Result<Schedule> {
        val scheduleResult = getSchedule(cinemaId, matcher)
        if (scheduleResult is Success) {
            getLocation(cinemaId).let {
                if (it is Success) {
                    scheduleResult.data.cinema.location = it.data
                }
            }
        }
        return scheduleResult
    }

    /**
     * Retrieves tickets data for a given cinema.
     *
     * @param cinemaId ID of the desired cinema.
     */
    suspend fun getTickets(cinemaId: Int): Result<Tickets> {
        val cached = ticketsCache[cinemaId]
        return if (cached != null) {
            Success(cached)
        } else {
            getRemoteAndCache(
                call = { remoteDataSource.getTickets(cinemaId) },
                cacheMap = ticketsCache,
                entryKey = cinemaId
            )
        }
    }

    fun clearSchedule(cinemaId: Int) {
        scheduleCache.remove(cinemaId)
    }
}