package com.diegobezerra.cinemaisapp.ui.tickets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diegobezerra.core.cinemais.data.cinemas.CinemasRepository
import com.diegobezerra.core.cinemais.domain.model.Tickets
import com.diegobezerra.core.util.RxUtils
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class TicketsViewModel @Inject constructor(
    private val cinemasRepository: CinemasRepository
) : ViewModel() {

    private val disposables = CompositeDisposable()

    private var cinemaId = MutableLiveData<Int>()

    private val _tickets = MediatorLiveData<Tickets>()
    val tickets: LiveData<Tickets>
        get() = _tickets

    init {
        _tickets.addSource(cinemaId) {
            fetchTickets()
        }
    }

    override fun onCleared() {
        disposables.clear()
    }

    private fun fetchTickets() {
        disposables.add(
            RxUtils.getSingle(cinemasRepository.getTickets(cinemaId.value!!))
                .subscribe { tickets, throwable ->
                    _tickets.value = tickets
                })
    }

    fun postCinemaId(id: Int) {
        if (cinemaId.value != id) {
            cinemaId.postValue(id)
        }
    }
}