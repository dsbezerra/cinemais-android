package com.diegobezerra.cinemaisapp.ui.movie.playingcinemas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diegobezerra.cinemaisapp.util.postValueIfNew
import com.diegobezerra.cinemaisapp.util.setValueIfNew
import com.diegobezerra.core.cinemais.data.cinemas.CinemasRepository
import com.diegobezerra.core.cinemais.data.movie.MoviesRepository
import com.diegobezerra.core.cinemais.domain.model.Cinema
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.util.DateUtils
import com.diegobezerra.core.util.RxUtils
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class PlayingCinemasViewModel @Inject constructor(
    private val cinemasRepository: CinemasRepository,
    private val moviesRepository: MoviesRepository
) : ViewModel(), PlayingCinemasEventListener {

    companion object {

        const val STATE_PLAYING_ROOMS = 0
        const val STATE_SCHEDULE = 1

        const val LOADING_NONE = -1
        const val LOADING_PLAYING_ROOMS = 0
        const val LOADING_SCHEDULE = 1
    }


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

    private val _state = MediatorLiveData<Int>().apply {
        value = STATE_PLAYING_ROOMS
    }
    val state: LiveData<Int>
        get() = _state

    private var movieId = MutableLiveData<Int>()

    private var cinemaId = MutableLiveData<Int>()

    private val dateString = MutableLiveData<String>(DateUtils.dateAsString())

    private var firstInitialization: Boolean = true

    private val disposables = CompositeDisposable()

    init {

        _cinemas.addSource(movieId) {
            refreshPlayingCinemas()
        }
        _schedule.addSource(cinemaId) {
            refreshSchedule()
        }

        // This takes care of refreshing schedule/playing rooms in case the date changes.
        _cinemas.addSource(dateString) {
            if (!firstInitialization) {
                refreshPlayingCinemas(true)
            }
            firstInitialization = false
        }
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

    private fun refreshPlayingCinemas(remote: Boolean = false) {
        getMovieId()?.let {
            if (remote) {
                moviesRepository.clearMovieWithId(it)
            }
            disposables.add(
                RxUtils.getSingle(moviesRepository.getPlayingCinemas(it))
                    .doOnSubscribe {
                        _loading.value = LOADING_PLAYING_ROOMS
                    }
                    .doOnSuccess {
                        _loading.value = LOADING_NONE
                    }
                    .doOnError { throwable ->
                        _loading.value = LOADING_NONE
                        // TODO: Implement error handling
                        Timber.d("throwable=$throwable")
                    }
                    .subscribe { cinemas ->
                        _cinemas.value = cinemas
                    })
        }
    }

    private fun refreshSchedule(remote: Boolean = false) {
        getCinemaId()?.let {
            if (remote) {
                cinemasRepository.clearSchedule(it)
            }
            disposables.add(
                RxUtils.getSingle(cinemasRepository.getSchedule(it))
                    .doOnSubscribe {
                        _loading.value = LOADING_SCHEDULE
                    }
                    .doOnSuccess {
                        _loading.value = LOADING_NONE
                    }
                    .doOnError { throwable ->
                        _loading.value = LOADING_NONE
                        // TODO: Implement error handling
                        Timber.d("throwable=$throwable")
                    }
                    .subscribe { schedule ->
                        _schedule.value = schedule
                        _currentCinema.value = schedule.cinema
                    }
            )
        }
    }

    private fun getMovieId(): Int? {
        return movieId.value
    }

    private fun getCinemaId(): Int? {
        return cinemaId.value
    }

    override fun onReady(movie: Int) {
        movieId.setValueIfNew(movie)
    }

    override fun onCinemaClicked(cinema: Int) {
        cinemaId.setValueIfNew(cinema)
        _state.postValueIfNew(STATE_SCHEDULE)
        _schedule.value?.let {
            _currentCinema.setValueIfNew(it.cinema)
        }

    }

    override fun onBackClicked() {
        _state.postValueIfNew(STATE_PLAYING_ROOMS)
        _currentCinema.postValueIfNew(null)
    }
}

interface PlayingCinemasEventListener {

    fun onReady(movie: Int)

    fun onBackClicked()

    fun onCinemaClicked(cinema: Int)

}