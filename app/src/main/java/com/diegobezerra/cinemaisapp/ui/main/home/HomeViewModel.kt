package com.diegobezerra.cinemaisapp.ui.main.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.diegobezerra.cinemaisapp.base.BaseViewModel
import com.diegobezerra.core.cinemais.data.home.HomeRepository
import com.diegobezerra.core.cinemais.domain.model.Home
import com.diegobezerra.shared.result.Event

class HomeViewModel @ViewModelInject constructor(
    private val homeRepository: HomeRepository
) : BaseViewModel(), HomeEventListener {

    private val _home = MediatorLiveData<Home>()
    val home: LiveData<Home>
        get() = _home

    private val _navigateToMovieDetail = MutableLiveData<Event<Int>>()
    val navigateToMovieDetail: LiveData<Event<Int>>
        get() = _navigateToMovieDetail

    private val _navigateToAllUpcomingMovies = MutableLiveData<Event<Unit>>()
    val navigateToAllUpcomingMovies: LiveData<Event<Unit>>
        get() = _navigateToAllUpcomingMovies

    init {
        refreshHome()
    }

    private fun refreshHome() {
        execute({ homeRepository.getHome() },
            onSuccess = {
                _home.value = it
            },
            onError = {
                // No-op
            })
    }

    override fun onMovieClicked(movieId: Int) {
        _navigateToMovieDetail.postValue(Event(movieId))
    }

    override fun onShowAllUpcomingClicked() {
        _navigateToAllUpcomingMovies.postValue(Event(Unit))
    }
}

interface HomeEventListener {

    fun onMovieClicked(movieId: Int)

    fun onShowAllUpcomingClicked()

}