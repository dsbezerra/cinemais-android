package com.diegobezerra.cinemaisapp.ui.cinema

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diegobezerra.cinemaisapp.util.setValueIfNew
import com.diegobezerra.core.cinemais.data.cinemas.CinemasRepository
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.util.DateUtils
import com.diegobezerra.core.util.RxUtils
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class CinemaViewModel @Inject constructor(
    private val cinemasRepository: CinemasRepository
) : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _schedule = MediatorLiveData<Schedule>()
    val schedule: LiveData<Schedule>
        get() = _schedule

    private val dateString = MutableLiveData<String>(DateUtils.dateAsString())

    private val cinemaId = MutableLiveData<Int>()

    private val disposables = CompositeDisposable()

    private var firstInitialization: Boolean = true

    init {

        _schedule.addSource(cinemaId) {
            refreshSchedule()
        }

        // This takes care of refreshing schedule in case the date changes.
        _schedule.addSource(dateString) {
            if (!firstInitialization) {
                refreshSchedule(true)
            }
            firstInitialization = false
        }

    }

    override fun onCleared() {
        disposables.clear()
    }

    private fun refreshSchedule(forceRemote: Boolean = false) {
        getCinemaId()?.let {
            if (forceRemote) {
                cinemasRepository.clearSchedule(it)
            }
            disposables.add(RxUtils.getSingle(cinemasRepository.getScheduleWithLocation(it))
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
                    _schedule.value = schedule
                })
        }
    }

    fun setDateString() {
        dateString.setValueIfNew(DateUtils.dateAsString())
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