package com.diegobezerra.cinemaisapp.ui.movie

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


class MovieViewModel @Inject constructor(
    private val moviesRespository: MoviesRepository
) : ViewModel() {

    private val disposables = CompositeDisposable()

    private var movieId = MutableLiveData<Int>()

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _movie = MediatorLiveData<Movie>()
    val movie: LiveData<Movie>
        get() = _movie

    init {
        _movie.addSource(movieId) {
            fetchMovie(false)
        }
    }

    override fun onCleared() {
        disposables.clear()
    }

    fun refresh() {
        fetchMovie(true)
    }

    private fun fetchMovie(forceRemote: Boolean): LiveData<Movie> {
        val id = movieId.value!!

        if (forceRemote) { moviesRespository.clearMovieWithId(id) }

        disposables.add(RxUtils.getSingle(moviesRespository.getMovie(id))
            .doOnSubscribe { _loading.value = true }
            .doOnSuccess { _loading.value = false }
            .doOnError { _loading.value = false }
            .subscribe(
                { _movie.value = it },
                { e -> Timber.e(e)}
            ))
        return movie
    }

    fun setMovieId(id: Int) {
        if (id != movieId.value) {
            movieId.value = id
        }
    }
}
