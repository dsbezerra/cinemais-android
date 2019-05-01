package com.diegobezerra.cinemaisapp.ui.cinema

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper
import com.diegobezerra.core.cinemais.data.cinemas.CinemasRepository
import com.diegobezerra.core.cinemais.domain.model.Cinema
import com.diegobezerra.core.cinemais.domain.model.Location
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.cinemais.domain.model.ScheduleDay
import com.diegobezerra.core.util.RxUtils
import com.diegobezerra.core.util.RxUtils.Companion
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class CinemaViewModel @Inject constructor(
    private val cinemasRespository: CinemasRepository
) : ViewModel() {

    private val disposables = CompositeDisposable()

    private var cinemaId = MutableLiveData<Int>()

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _schedule = MediatorLiveData<Schedule>()
    val schedule: LiveData<Schedule>
        get() = _schedule

//    private val _scheduleDays: MutableList<LiveData<ScheduleDay>> = mutableListOf()

//    private var originalSchedule: Schedule? = null

    init {
        _schedule.addSource(cinemaId) {
            fetchSchedule(false)
        }
    }

    override fun onCleared() {
        disposables.clear()
    }

    private fun fetchSchedule(forceRemote: Boolean) {
        val id = cinemaId.value!!

        disposables.add(RxUtils.getSingle(cinemasRespository.getScheduleWithLocation(id))
            .doOnSubscribe { _loading.value = true }
            .doOnSuccess { _loading.value = false }
            .subscribe { schedule, throwable ->
//                originalSchedule = schedule
                _schedule.value = schedule
//                _scheduleDays.clear()
//                for (day in schedule.days) {
//                    _scheduleDays.add(MutableLiveData(day))
//                }
            })
    }

    fun setCinemaId(id: Int) {
        if (id != cinemaId.value) {
            cinemaId.value = id
        }
    }

}