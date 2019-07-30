package com.diegobezerra.cinemaisapp.ui.main.cinemas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.diegobezerra.cinemaisapp.base.BaseViewModel
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper
import com.diegobezerra.core.cinemais.data.cinemas.CinemaRepository
import com.diegobezerra.core.cinemais.domain.model.Cinema
import com.diegobezerra.core.event.Event
import javax.inject.Inject

class CinemasViewModel @Inject constructor(
    private val cinemaRepository: CinemaRepository,
    private val preferencesHelper: PreferencesHelper
) : BaseViewModel(), CinemasEventListener {

    private val _cinemas = MediatorLiveData<List<Cinema>>()
    val cinemas: LiveData<List<Cinema>>
        get() = _cinemas

    private val _switchToCinemaDetail = MutableLiveData<Event<Int>>()
    val switchToCinemaDetail: LiveData<Event<Int>>
        get() = _switchToCinemaDetail

    init {
        loadCinemas()
    }

    private fun loadCinemas() {
        execute({ cinemaRepository.getCinemas() },
            onSuccess = {
                _cinemas.value = it.cinemas
            },
            onError = {
                // No-op
            })
    }

    override fun onCinemaClicked(cinemaId: Int) {
        preferencesHelper.saveSelectedCinemaId(cinemaId)
        _switchToCinemaDetail.postValue(Event(cinemaId))
    }
}

interface CinemasEventListener {

    fun onCinemaClicked(cinemaId: Int)

}