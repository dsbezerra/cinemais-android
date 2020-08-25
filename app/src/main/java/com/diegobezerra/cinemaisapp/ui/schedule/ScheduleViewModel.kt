package com.diegobezerra.cinemaisapp.ui.schedule

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.diegobezerra.cinemaisapp.base.BaseViewModel
import com.diegobezerra.cinemaisapp.util.setValueIfNew
import com.diegobezerra.core.cinemais.data.cinemas.CinemaRepository
import com.diegobezerra.core.cinemais.domain.model.Schedule

class ScheduleViewModel @ViewModelInject constructor(
    private val cinemaRepository: CinemaRepository
) : BaseViewModel() {

    private val _schedule = MediatorLiveData<Schedule>()
    val schedule: LiveData<Schedule>
        get() = _schedule

    private val cinemaId = MutableLiveData<Int>()

    init {

        _schedule.addSource(cinemaId) {
            refreshSchedule()
        }
    }

    private fun refreshSchedule() {
        getCinemaId()?.let { cinemaId ->
            execute({ cinemaRepository.getSchedule(cinemaId) },
                onSuccess = {
                    _schedule.value = it
                },
                onError = {
                    // No-op
                })
        }
    }

    /**
     * Sets the current cinema ID only if it's new.
     */
    fun setCinemaId(newCinemaId: Int) {
        cinemaId.setValueIfNew(newCinemaId)
    }

    /**
     * Returns the current cinema ID or null if not available.
     */
    private fun getCinemaId(): Int? = cinemaId.value
}