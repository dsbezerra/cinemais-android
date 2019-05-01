package com.diegobezerra.cinemaisapp.ui.movie.playingcinemas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diegobezerra.core.cinemais.data.cinemas.CinemasRepository
import com.diegobezerra.core.cinemais.data.movie.MoviesRepository
import com.diegobezerra.core.cinemais.domain.model.Cinema
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.util.RxUtils
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class PlayingCinemasViewModel@Inject constructor(
    private val cinemasRepository: CinemasRepository,
    private val moviesRespository: MoviesRepository
) : ViewModel(), PlayingCinemasEventListener {

    companion object {

        const val STATE_PLAYING_ROOMS = 0
        const val STATE_SCHEDULE = 1

        const val LOADING_NONE = -1
        const val LOADING_PLAYING_ROOMS = 0
        const val LOADING_SCHEDULE = 1
    }

    private val disposables = CompositeDisposable()

    private var movieId = MutableLiveData<Int>()
    private var cinemaId = MutableLiveData<Int>()

    private var _loading = MutableLiveData<Int>()
    val loading: LiveData<Int>
        get() = _loading

    private val _cinemas = MediatorLiveData<List<Cinema>>()
    val cinemas: LiveData<List<Cinema>>
        get() = _cinemas

    private val _schedule = MediatorLiveData<Schedule>()
    val schedule: LiveData<Schedule>
        get() = _schedule

    private var _currentCinema = MutableLiveData<Cinema?>()
    val currentCinema: LiveData<Cinema?>
        get() = _currentCinema

    private val _state = MediatorLiveData<Int>()
    val state: LiveData<Int>
        get() = _state

    init {
        _state.value = STATE_PLAYING_ROOMS
        _cinemas.addSource(movieId) {
            fetchPlayingCinemas(it)
        }
        _schedule.addSource(cinemaId) {
            fetchSchedule(it)
        }
    }

    override fun onCleared() {
        disposables.clear()
    }

    private fun fetchPlayingCinemas(movieId: Int) {
        disposables.add(
            RxUtils.getSingle(moviesRespository.getPlayingCinemas(movieId))
                .doOnSubscribe {
                    _loading.value = LOADING_PLAYING_ROOMS
                }
                .doOnSuccess {
                    _loading.value = LOADING_NONE
                }
                .doOnError {
                    _loading.value = LOADING_NONE
                }
                .subscribe(
                    { _cinemas.value = it },
                    { e -> Timber.e(e)}
                ))
    }

    private fun fetchSchedule(cinemaId: Int) {
        disposables.add(
            RxUtils.getSingle(cinemasRepository.getSchedule(cinemaId))
                .doOnSubscribe {
                    _loading.value = LOADING_SCHEDULE
                }
                .doOnSuccess {
                    _loading.value = LOADING_NONE
                }
                .doOnError {
                    _loading.value = LOADING_NONE
                }
                .subscribe({
                    _schedule.value = it
                    _currentCinema.value = it.cinema
                }, {
                    Timber.e(it)
                }))
    }

    override fun onReady(movieId: Int) {
        if (this.movieId.value != movieId) {
            this.movieId.value = movieId
        }
    }

    override fun onCinemaClicked(id: Int) {
        if (cinemaId.value != id) {
            cinemaId.value = id
        } else {
            _currentCinema.value = _schedule.value?.cinema
        }

        if (_state.value != STATE_SCHEDULE) {
            _state.postValue(STATE_SCHEDULE)
        }
    }

    override fun onBackClicked() {
        if (_state.value != STATE_PLAYING_ROOMS) {
            _state.postValue(STATE_PLAYING_ROOMS)
            _currentCinema.postValue(null)
        }
    }
}

interface PlayingCinemasEventListener {

    fun onReady(movieId: Int)

    fun onBackClicked()

    fun onCinemaClicked(id: Int)

}