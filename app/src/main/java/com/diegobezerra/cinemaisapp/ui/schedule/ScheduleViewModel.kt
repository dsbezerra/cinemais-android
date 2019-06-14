package com.diegobezerra.cinemaisapp.ui.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diegobezerra.cinemaisapp.util.setValueIfNew
import com.diegobezerra.core.cinemais.data.cinemas.CinemasRepository
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.cinemais.domain.model.ScheduleDay
import com.diegobezerra.core.util.RxUtils
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class ScheduleViewModel @Inject constructor(
    private val cinemasRepository: CinemasRepository
) : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _schedule = MediatorLiveData<Schedule>()
    val schedule: LiveData<Schedule>
        get() = _schedule

    private val _scheduleDays = mutableListOf<LiveData<ScheduleDay>>()

    private val cinemaId = MutableLiveData<Int>()

    private val disposables = CompositeDisposable()

    init {

        _schedule.addSource(cinemaId) {
            refreshSchedule()
        }
    }

    override fun onCleared() {
        disposables.clear()
    }

    private fun refreshSchedule() {
        getCinemaId()?.let {
            disposables.add(
                RxUtils.getSingle(cinemasRepository.getSchedule(it))
                    .doOnSubscribe {
                        _loading.value = true
                    }
                    .doOnSuccess {
                        _loading.value = false
                    }
                    .doOnError { throwable ->
                        _loading.value = false
                        // TODO: Implement error handling
                        Timber.d("throwable=$throwable")
                    }
                    .subscribe { schedule, _ ->
                        _scheduleDays.clear()
                        for (day in schedule.days) {
                            _scheduleDays.add(MutableLiveData(day))
                        }
                        _schedule.value = schedule
                    })
        }
    }

    fun getScheduleForDay(day: Int): LiveData<ScheduleDay>? {
        if (day >= 0 && day < _scheduleDays.size) {
            return _scheduleDays[day]
        }
        return null
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