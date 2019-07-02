package com.diegobezerra.cinemaisapp.ui.cinema

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.diegobezerra.cinemaisapp.base.BaseViewModel
import com.diegobezerra.cinemaisapp.util.setValueIfNew
import com.diegobezerra.core.cinemais.data.cinemas.CinemaRepository
import com.diegobezerra.core.cinemais.domain.model.Cinema
import com.diegobezerra.core.cinemais.domain.model.Location
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.event.Event
import javax.inject.Inject

class CinemaViewModel @Inject constructor(
    private val cinemaRepository: CinemaRepository
) : BaseViewModel() {

    private val _cinema = MediatorLiveData<Cinema>()
    val cinema: LiveData<Cinema>
        get() = _cinema

    private val _schedule = MediatorLiveData<Schedule>()
    val schedule: LiveData<Schedule>
        get() = _schedule

    private val _navigateToSchedulePageAction = MutableLiveData<Event<Int>>()
    val navigateToSchedulePageAction: LiveData<Event<Int>>
        get() = _navigateToSchedulePageAction

    private val _navigateToTicketsAction = MutableLiveData<Event<Int>>()
    val navigateToTicketsAction: LiveData<Event<Int>>
        get() = _navigateToTicketsAction

    private val _navigateToLocationAction = MutableLiveData<Event<Location>>()
    val navigateToLocationAction: LiveData<Event<Location>>
        get() = _navigateToLocationAction

    private val _navigateToInfoAction = MutableLiveData<Event<Unit>>()
    val navigateToInfoAction: LiveData<Event<Unit>>
        get() = _navigateToInfoAction

    private val cinemaId = MutableLiveData<Int>()

    init {
        _schedule.addSource(cinemaId) {
            refreshSchedule()
        }
    }

    private fun refreshSchedule(ignoreCache: Boolean = false) {
        getCinemaId()?.let {
            if (ignoreCache) {
                cinemaRepository.clearSchedule(it)
            }
            execute(
                { cinemaRepository.getScheduleWithLocation(it) },
                onSuccess = { schedule ->
                    _cinema.value = schedule.cinema
                    _schedule.value = schedule
                },
                onError = {
                    // No-op
                })
        }
    }

    fun onSeeScheduleInWebsite() {
        getCinemaId()?.let {
            _navigateToSchedulePageAction.value = Event(it)
        }
    }

    fun onTicketsClicked() {
        getCinemaId()?.let {
            _navigateToTicketsAction.value = Event(it)
        }
    }

    fun onLocationClicked() {
        getCinemaLocation()?.let {
            _navigateToLocationAction.value = Event(it)
        }
    }

    fun onInfoClicked() {
        _navigateToInfoAction.value = Event(Unit)
    }

    /**
     * Sets the current cinema ID only if it's new.
     */
    fun setCinemaId(newCinemaId: Int?) {
        cinemaId.setValueIfNew(newCinemaId)
    }

    /**
     * Returns the current cinema ID or null if not available.
     */
    private fun getCinemaId(): Int? = cinemaId.value

    /**
     * Returns the current cinema's location or null if not available.
     */
    private fun getCinemaLocation(): Location? {
        return cinema.value?.location
    }

}