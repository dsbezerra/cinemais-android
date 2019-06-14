package com.diegobezerra.cinemaisapp.ui.tickets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diegobezerra.cinemaisapp.util.setValueIfNew
import com.diegobezerra.core.cinemais.data.cinemas.CinemasRepository
import com.diegobezerra.core.cinemais.domain.model.Tickets
import com.diegobezerra.core.util.RxUtils
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class TicketsViewModel @Inject constructor(
    private val cinemasRepository: CinemasRepository
) : ViewModel() {

    private val _loading = MediatorLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _tickets = MediatorLiveData<Tickets>()
    val tickets: LiveData<Tickets>
        get() = _tickets

    private val cinemaId = MutableLiveData<Int>()

    private val disposables = CompositeDisposable()

    init {
        // Refresh tickets every time cinemaId's value is changed.
        _tickets.addSource(cinemaId) {
            refreshTickets()
        }
    }

    override fun onCleared() {
        disposables.clear()
    }

    /**
     * Refresh tickets data for current cinema ID.
     */
    private fun refreshTickets() {
        getCinemaId()?.let {
            disposables.add(
                RxUtils.getSingle(cinemasRepository.getTickets(it))
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
                    .subscribe({ tickets ->
                        _tickets.value = tickets
                    }, { throwable ->
                        Timber.d("throwable=$throwable")
                    })
            )
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