package com.diegobezerra.cinemaisapp.ui.cinema

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.diegobezerra.cinemaisapp.base.BaseViewModel
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper
import com.diegobezerra.cinemaisapp.ui.schedule.filters.FilterableSchedule
import com.diegobezerra.cinemaisapp.ui.schedule.filters.ScheduleFilter
import com.diegobezerra.cinemaisapp.ui.schedule.filters.ScheduleFilter.Companion.createFilter
import com.diegobezerra.cinemaisapp.util.setValueIfNew
import com.diegobezerra.core.cinemais.data.cinemas.CinemaRepository
import com.diegobezerra.core.cinemais.domain.model.Cinema
import com.diegobezerra.core.cinemais.domain.model.Location
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.cinemais.domain.model.Session.Companion.RoomMagicD
import com.diegobezerra.core.cinemais.domain.model.Session.Companion.RoomVIP
import com.diegobezerra.core.cinemais.domain.model.Session.Companion.VersionDubbed
import com.diegobezerra.core.cinemais.domain.model.Session.Companion.VersionNational
import com.diegobezerra.core.cinemais.domain.model.Session.Companion.VersionSubtitled
import com.diegobezerra.core.cinemais.domain.model.Session.Companion.VideoFormat2D
import com.diegobezerra.core.cinemais.domain.model.Session.Companion.VideoFormat3D
import com.diegobezerra.core.cinemais.domain.model.SessionMatcher
import com.diegobezerra.core.event.Event
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class CinemaViewModel @Inject constructor(
    private val cinemaRepository: CinemaRepository,
    private val preferencesHelper: PreferencesHelper
) : BaseViewModel(), FilterableSchedule {

    private val _cinema = MediatorLiveData<Cinema>()
    val cinema: LiveData<Cinema>
        get() = _cinema

    private val _schedule = MediatorLiveData<Schedule>()
    val schedule: LiveData<Schedule>
        get() = _schedule

    private val _navigateToSchedulePageAction = MutableLiveData<Event<Int>>()
    val navigateToSchedulePageAction: LiveData<Event<Int>>
        get() = _navigateToSchedulePageAction

    private val _navigateToTicketsAction = MutableLiveData<Event<Int>>()
    val navigateToTicketsAction: LiveData<Event<Int>>
        get() = _navigateToTicketsAction

    private val _navigateToLocationAction = MutableLiveData<Event<Location>>()
    val navigateToLocationAction: LiveData<Event<Location>>
        get() = _navigateToLocationAction

    private val _navigateToInfoAction = MutableLiveData<Event<Unit>>()
    val navigateToInfoAction: LiveData<Event<Unit>>
        get() = _navigateToInfoAction

    private val _filters = MutableLiveData<List<ScheduleFilter>>()
    val filters: LiveData<List<ScheduleFilter>>
        get() = _filters

    private val selectedFilters: HashSet<String> = preferencesHelper.getSelectedFilters()

    val isCinemaLayoutVisible = ObservableBoolean()

    val isFilterVisible = ObservableBoolean()

    val isFilterEnabled = ObservableBoolean()

    val isScheduleEmpty = ObservableBoolean()

    private val cinemaId = MutableLiveData<Int>()

    init {
        createFilters()

        _schedule.addSource(cinemaId) {
            refreshSchedule(
                filtering = selectedFilters.isNotEmpty(),
                cinemaChanged = true
            )
        }
    }

    private fun refreshSchedule(
        filtering: Boolean = false,
        cinemaChanged: Boolean = false,
        ignoreCache: Boolean = false
    ) {
        getCinemaId()?.let {
            updateLayoutVisibility(filtering, cinemaChanged)
            if (ignoreCache) {
                cinemaRepository.clearSchedule(it)
            }
            execute(
                { cinemaRepository.getScheduleWithLocation(it, SessionMatcher(selectedFilters)) },
                onSuccess = { schedule ->
                    _cinema.value = schedule.cinema
                    _schedule.value = schedule
                    isScheduleEmpty.set(schedule.days.isEmpty())
                    updateLayoutVisibility()
                },
                onError = {
                    // No-op
                    updateLayoutVisibility()
                })
        }
    }

    fun onSeeScheduleInWebsite() {
        getCinemaId()?.let {
            _navigateToSchedulePageAction.value = Event(it)
        }
    }

    fun onTicketsClicked() {
        getCinemaId()?.let {
            _navigateToTicketsAction.value = Event(it)
        }
    }

    fun onLocationClicked() {
        getCinemaLocation()?.let {
            _navigateToLocationAction.value = Event(it)
        }
    }

    fun onInfoClicked() {
        _navigateToInfoAction.value = Event(Unit)
    }

    fun onFilterClick() = runBlocking {
        // NOTE(diego): Make sure ripple effect runs for a while...
        launch {
            delay(200L)
            isFilterVisible.set(!isFilterVisible.get())
        }
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

    private fun updateLayoutVisibility(
        filtering: Boolean = false,
        cinemaChanged: Boolean = false
    ) {
        if (isFilterEnabled.get() && filtering && !cinemaChanged) {
            isCinemaLayoutVisible.set(true)
        } else {
            val loading = if (loading.value == null || cinemaChanged) true else loading.value!!
            isCinemaLayoutVisible.set(!loading)
        }
    }

    private fun createFilters() {
        val filters = listOf(
            VersionDubbed,
            VersionSubtitled,
            VersionNational,
            VideoFormat2D,
            VideoFormat3D,
            RoomMagicD,
            RoomVIP
        ).map { createFilter(it, selectedFilters.contains(it)) }
        if (filters.any { it.isChecked.get() }) {
            isFilterEnabled.set(true)
            isFilterVisible.set(true)
        }
        _filters.setValueIfNew(filters)
    }

    /**
     * Sets the current cinema ID only if it's new.
     */
    fun setCinemaId(newCinemaId: Int?) {
        cinemaId.setValueIfNew(newCinemaId)
    }

    /**
     * Returns the current cinema ID or null if not available.
     */
    private fun getCinemaId(): Int? = cinemaId.value

    /**
     * Returns the current cinema's location or null if not available.
     */
    private fun getCinemaLocation(): Location? {
        return cinema.value?.location
    }

}