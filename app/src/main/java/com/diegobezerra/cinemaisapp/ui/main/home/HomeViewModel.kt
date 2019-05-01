package com.diegobezerra.cinemaisapp.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diegobezerra.core.cinemais.data.home.HomeRepository
import com.diegobezerra.core.cinemais.domain.model.HomeData
import com.diegobezerra.core.cinemais.domain.model.Movie
import com.diegobezerra.core.event.Event
import com.diegobezerra.core.util.RxUtils
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel(), HomeEventListener {

    private val disposables = CompositeDisposable()

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _home = MediatorLiveData<HomeData>()
    val home: LiveData<HomeData>
        get() = _home

    private val _navigateToMovieDetail = MutableLiveData<Event<Int>>()
    val navigateToMovieDetail: LiveData<Event<Int>>
        get() = _navigateToMovieDetail

    private val _navigateToAllUpcomingMovies = MutableLiveData<Event<Unit>>()
    val navigateToAllUpcomingMovies: LiveData<Event<Unit>>
        get() = _navigateToAllUpcomingMovies

    init {
        fetchHome()
    }

    override fun onCleared() {
        disposables.clear()
    }

    private fun fetchHome(): LiveData<HomeData> {
        disposables.add(
            RxUtils.getSingle(homeRepository.getHome())
            .doOnSubscribe { _loading.value = true }
            .doOnSuccess { _loading.value = false }
            .doOnError { _loading.value = false }
            .subscribe(
                { _home.value = it },
                { e -> Timber.e(e)}
            ))
        return home
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