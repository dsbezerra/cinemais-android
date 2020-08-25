package com.diegobezerra.cinemaisapp.ui.tickets

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.diegobezerra.cinemaisapp.base.BaseViewModel
import com.diegobezerra.cinemaisapp.util.setValueIfNew
import com.diegobezerra.core.cinemais.data.cinemas.CinemaRepository
import com.diegobezerra.core.cinemais.domain.model.Tickets
import com.diegobezerra.shared.result.Event

class TicketsViewModel @ViewModelInject constructor(
    private val cinemaRepository: CinemaRepository
) : BaseViewModel() {

    private val _tickets = MediatorLiveData<Tickets>()
    val tickets: LiveData<Tickets>
        get() = _tickets

    private val _navigateToBuyWebsiteAction = MediatorLiveData<Event<String>>()
    val navigateToBuyWebsiteAction: LiveData<Event<String>>
        get() = _navigateToBuyWebsiteAction

    private val cinemaId = MutableLiveData<Int>()

    init {
        // Refresh tickets every time cinemaId's value is changed.
        _tickets.addSource(cinemaId) {
            refreshTickets()
        }
    }

    /**
     * Refresh tickets data for current cinema ID.
     */
    private fun refreshTickets() {
        getCinemaId()?.let { cinemaId ->
            execute({ cinemaRepository.getTickets(cinemaId) },
                onSuccess = {
                    _tickets.value = it
                },
                onError = {
                    // No-op
                })
        }
    }

    fun onBuyOnlineClicked() {
        getTicketsBuyUrl()?.let {
            _navigateToBuyWebsiteAction.value = Event(it)
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

    /**
     * Returns the current tickets or null if not available.
     */
    private fun getTickets(): Tickets? = tickets.value

    /**
     * Returns the current tickets buy url or null if not available.
     */
    private fun getTicketsBuyUrl(): String? = getTickets()?.buyOnlineUrl

}