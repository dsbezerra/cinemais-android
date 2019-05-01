package com.diegobezerra.cinemaisapp.ui.main.cinemas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper
import com.diegobezerra.core.cinemais.data.cinemas.CinemasRepository
import com.diegobezerra.core.cinemais.domain.model.Cinema
import com.diegobezerra.core.event.Event
import com.diegobezerra.core.util.RxUtils
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class CinemasViewModel @Inject constructor(
    private val cinemasRepository: CinemasRepository,
    private val preferencesHelper: PreferencesHelper
) : ViewModel(), CinemasEventListener {

    private val disposables = CompositeDisposable()

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _cinemas = MediatorLiveData<List<Cinema>>()
    val cinemas: LiveData<List<Cinema>>
        get() = _cinemas

    private val _switchToCinemaDetail = MutableLiveData<Event<Int>>()
    val switchToCinemaDetail: LiveData<Event<Int>>
        get() = _switchToCinemaDetail

    init {
        loadCinemas()
    }

    override fun onCleared() {
        disposables.clear()
    }

    private fun loadCinemas() {
        disposables.add(RxUtils.getSingle(cinemasRepository.getCinemas())
            .doOnSubscribe { _loading.value = true }
            .doOnSuccess { _loading.value = false }
            .doOnError { _loading.value = false }
            .subscribe(
                { _cinemas.value = it.cinemas },
                { e -> Timber.e(e)}
            ))
    }

    override fun onCinemaClicked(cinemaId: Int) {
        preferencesHelper.setSelectedCinemaId(cinemaId)
        _switchToCinemaDetail.postValue(Event(cinemaId))
    }
}

interface CinemasEventListener {

    fun onCinemaClicked(cinemaId: Int)

}