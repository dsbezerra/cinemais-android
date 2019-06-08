package com.diegobezerra.cinemaisapp.ui.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diegobezerra.core.cinemais.data.cinemas.CinemasRepository
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.cinemais.domain.model.ScheduleDay
import com.diegobezerra.core.util.RxUtils
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class ScheduleViewModel @Inject constructor(
    private val cinemasRepository: CinemasRepository
) : ViewModel() {

    private val disposables = CompositeDisposable()

    private var cinemaId = MutableLiveData<Int>()

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _schedule = MediatorLiveData<Schedule>()
    val schedule: LiveData<Schedule>
        get() = _schedule

    private val _scheduleDays = mutableListOf<LiveData<ScheduleDay>>()

    init {
        _schedule.addSource(cinemaId) {
            fetchSchedule()
        }
    }

    override fun onCleared() {
        disposables.clear()
    }

    private fun fetchSchedule() {
        val id = cinemaId.value!!
        disposables.add(
            RxUtils.getSingle(cinemasRepository.getSchedule(id))
            .subscribe { schedule, throwable ->
                _scheduleDays.clear()
                for (day in schedule.days) {
                    _scheduleDays.add(MutableLiveData(day))
                }
                _schedule.value = schedule
            })
    }

    fun setCinema(cinema: Int) {
        if (cinemaId.value != cinema) {
            cinemaId.value = cinema
        }
    }

    fun getScheduleForDay(day: Int): LiveData<ScheduleDay>? {
        if (day >= 0 && day < _scheduleDays.size) {
            return _scheduleDays[day]
        }
        return null
    }
}