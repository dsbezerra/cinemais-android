package com.diegobezerra.cinemaisapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diegobezerra.shared.result.Event

class MainViewModel : ViewModel(), MainEventListener {

    private val _navigateToCinemaDetail = MutableLiveData<Event<Int>>()
    val navigateToCinemaDetail: LiveData<Event<Int>>
        get() = _navigateToCinemaDetail

    override fun onShowCinema(cinemaId: Int) {
        _navigateToCinemaDetail.postValue(Event(cinemaId))
    }
}

interface MainEventListener {

    fun onShowCinema(cinemaId: Int)

}