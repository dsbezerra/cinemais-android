package com.diegobezerra.cinemaisapp.ui.movie.playingcinemas

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.base.BaseViewModel
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper
import com.diegobezerra.cinemaisapp.ui.schedule.filters.FilterableSchedule
import com.diegobezerra.cinemaisapp.ui.schedule.filters.ScheduleFilter
import com.diegobezerra.cinemaisapp.util.setValueIfNew
import com.diegobezerra.core.cinemais.data.cinemas.CinemaRepository
import com.diegobezerra.core.cinemais.data.movie.MovieRepository
import com.diegobezerra.core.cinemais.domain.model.Cinema
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.cinemais.domain.model.Session
import com.diegobezerra.core.cinemais.domain.model.SessionMatcher
import com.diegobezerra.core.event.Event
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class PlayingCinemasViewModel @Inject constructor(
    private val cinemaRepository: CinemaRepository,
    private val movieRepository: MovieRepository,
    private val preferencesHelper: PreferencesHelper
) : BaseViewModel(), PlayingCinemasEventListener, FilterableSchedule {

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

    private val _filters = MutableLiveData<List<ScheduleFilter>>()
    val filters: LiveData<List<ScheduleFilter>>
        get() = _filters

    private val selectedFilters: HashSet<String> = preferencesHelper.getSelectedFilters()

    private val _toggleSheetAction = MutableLiveData<Event<Unit>>()
    val toggleSheetAction: LiveData<Event<Unit>>
        get() = _toggleSheetAction

    private var movieId = MutableLiveData<Int>()

    private var cinemaId = MutableLiveData<Int>()

    val isFilterVisible = ObservableBoolean()

    val isFilterEnabled = ObservableBoolean()

    val isViewingPlayingRooms = ObservableBoolean()

    val isViewingSchedule = ObservableBoolean()

    val isScheduleEmpty = ObservableBoolean()

    init {
        createFilters()

        _cinemas.addSource(movieId) {
            refreshPlayingCinemas()
        }
        _schedule.addSource(cinemaId) {
            refreshSchedule()
        }
    }

    private fun refreshPlayingCinemas(ignoreCache: Boolean = false) {
        getMovieId()?.let { movieId ->
            if (ignoreCache) {
                movieRepository.clearMovieWithId(movieId)
            }
            execute(
                { movieRepository.getPlayingCinemas(movieId) },
                onSuccess = {
                    _cinemas.value = it
                    setState(STATE_PLAYING_ROOMS)
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
            execute(
                {
                    cinemaRepository.getSchedule(
                        cinemaId,
                        SessionMatcher(selectedFilters, getMovieId())
                    )
                },
                onSuccess = { schedule ->
                    _schedule.value = schedule
                    _cinema.value = schedule.cinema
                    isScheduleEmpty.set(schedule.days.isEmpty())
                    setState(STATE_SCHEDULE)
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

    private fun setState(newState: Int) {
        _state.setValueIfNew(newState)
        isViewingPlayingRooms.set(newState == STATE_PLAYING_ROOMS)
        isViewingSchedule.set(newState == STATE_SCHEDULE)
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
        setState(STATE_PLAYING_ROOMS)
    }

    override fun onExpandOrCollapseClicked() {
        _toggleSheetAction.value = Event(Unit)
    }

    fun onFilterClick() = runBlocking {
        // NOTE(diego): Make sure ripple effect runs for a while...
        launch {
            delay(200L)
            isFilterVisible.set(!isFilterVisible.get())
        }
    }

    private fun createFilters() {
        val filters = listOf(
            Session.VersionDubbed,
            Session.VersionSubtitled,
            Session.VersionNational,
            Session.VideoFormat2D,
            Session.VideoFormat3D,
            Session.RoomMagicD,
            Session.RoomVIP
        ).map { ScheduleFilter.createFilter(it, selectedFilters.contains(it)) }
        if (filters.any { it.isChecked.get() }) {
            isFilterEnabled.set(true)
            isFilterVisible.set(true)
        }
        _filters.setValueIfNew(filters)
    }

    override fun onToggleFilter(filter: ScheduleFilter, checked: Boolean) {
        filter.isChecked.set(checked)
        if (checked) {
            selectedFilters.add(filter.id)
        } else {
            selectedFilters.remove(filter.id)
        }
        isFilterEnabled.set(selectedFilters.isNotEmpty())
        preferencesHelper.saveSelectedFilters(selectedFilters)
        refreshSchedule()
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