package com.diegobezerra.cinemaisapp.ui.movie.playingcinemas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.base.BaseViewModel
import com.diegobezerra.cinemaisapp.util.setValueIfNew
import com.diegobezerra.core.cinemais.data.cinemas.CinemaRepository
import com.diegobezerra.core.cinemais.data.movie.MovieRepository
import com.diegobezerra.core.cinemais.domain.model.Cinema
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.event.Event
import javax.inject.Inject

class PlayingCinemasViewModel @Inject constructor(
    private val cinemaRepository: CinemaRepository,
    private val movieRepository: MovieRepository
) : BaseViewModel(), PlayingCinemasEventListener {

    companion object {

        const val STATE_PLAYING_ROOMS = 0
        const val STATE_SCHEDULE = 1

    }

    private val _cinema = MutableLiveData<Cinema?>()
    val cinema: LiveData<Cinema?>
        get() = _cinema

    private val _cinemas = MediatorLiveData<List<Cinema>>()
    val cinemas: LiveData<List<Cinema>>
        get() = _cinemas

    private val _schedule = MediatorLiveData<Schedule>()
    val schedule: LiveData<Schedule>
        get() = _schedule

    private val _state = MutableLiveData(STATE_PLAYING_ROOMS)
    val state: LiveData<Int>
        get() = _state

    private val _toggleSheetAction = MutableLiveData<Event<Unit>>()
    val toggleSheetAction: LiveData<Event<Unit>>
        get() = _toggleSheetAction

    private var movieId = MutableLiveData<Int>()

    private var cinemaId = MutableLiveData<Int>()

    init {
        _cinemas.addSource(movieId) {
            refreshPlayingCinemas()
        }
        _schedule.addSource(cinemaId) {
            refreshSchedule()
        }
    }

    private fun refreshPlayingCinemas(ignoreCache: Boolean = false) {
        getMovieId()?.let { cinemaId ->
            if (ignoreCache) {
                movieRepository.clearMovieWithId(cinemaId)
            }
            execute({ movieRepository.getPlayingCinemas(cinemaId) },
                onSuccess = {
                    _cinemas.value = it
                    _state.setValueIfNew(STATE_PLAYING_ROOMS)
                },
                onError = {
                    // No-op
                })
        }
    }

    private fun refreshSchedule(ignoreCache: Boolean = false) {
        getCinemaId()?.let { cinemaId ->
            if (ignoreCache) {
                cinemaRepository.clearSchedule(cinemaId)
            }
            execute({ cinemaRepository.getSchedule(cinemaId) },
                onSuccess = { schedule ->
                    _schedule.value = schedule
                    _cinema.value = schedule.cinema
                    _state.setValueIfNew(STATE_SCHEDULE)
                },
                onError = {
                    // No-op
                })
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
        cinemaId.value = cinema
        _schedule.value?.let {
            _cinema.setValueIfNew(it.cinema)
        }
    }

    override fun onBackClicked() {
        _state.setValueIfNew(STATE_PLAYING_ROOMS)
    }

    override fun onExpandOrCollapseClicked() {
        _toggleSheetAction.value = Event(Unit)
    }

    fun getHeaderText(state: Int): Int {
        return when (state) {
            STATE_SCHEDULE -> R.string.header_sessions
            else -> R.string.header_playing_rooms
        }
    }
}

interface PlayingCinemasEventListener {

    fun onReady(movie: Int)

    fun onCinemaClicked(cinema: Int)

    fun onBackClicked()

    fun onExpandOrCollapseClicked()

}