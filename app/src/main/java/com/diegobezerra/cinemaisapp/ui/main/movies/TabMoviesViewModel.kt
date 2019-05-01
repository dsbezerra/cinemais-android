package com.diegobezerra.cinemaisapp.ui.main.movies

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diegobezerra.core.cinemais.data.movie.MoviesRepository
import com.diegobezerra.core.cinemais.domain.model.Movie
import com.diegobezerra.core.event.Event
import com.diegobezerra.core.util.RxUtils
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class TabMoviesViewModel @Inject constructor(
    private val moviesRepository: MoviesRepository
) : ViewModel(), MoviesEventListener {

    private val disposables = CompositeDisposable()

    private var _type = MutableLiveData<Int>()

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _movies = MediatorLiveData<List<Movie>>()
    val movies: LiveData<List<Movie>>
        get() = _movies

    private val _navigateToMovieDetail = MutableLiveData<Event<Int>>()
    val navigateToMovieDetail: LiveData<Event<Int>>
        get() = _navigateToMovieDetail

    init {
        _movies.addSource(_type) {
            loadMovies(false)
        }
    }

    override fun onCleared() {
        disposables.clear()
    }

    fun refresh() {
        loadMovies(true)
    }

    private fun loadMovies(forceRemote: Boolean) {
        val type = _type.value!!
        if (forceRemote) {
            moviesRepository.clearMovies(type)
        }

        val single = if (type == MoviesRepository.NOW_PLAYING) {
            moviesRepository.getNowPlaying()
        } else {
            moviesRepository.getUpcoming()
        }
        disposables.add(RxUtils.getSingle(single)
            .doOnSubscribe {
                _loading.postValue(true)
            }
            .doOnError {
                _loading.postValue(false)
            }
            .subscribe(
                {
                    _loading.value = false
                    _movies.value = it
                },
                {
                    Timber.e(it.message)
                }
            ))
    }

    fun setType(type: Int) {
        if (_type.value != type) {
            _type.value = type
        }
    }

    override fun onMovieClicked(movieId: Int) {
        _navigateToMovieDetail.postValue(Event(movieId))
    }
}

interface MoviesEventListener {

    fun onMovieClicked(movieId: Int)

}