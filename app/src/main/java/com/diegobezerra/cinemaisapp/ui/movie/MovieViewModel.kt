package com.diegobezerra.cinemaisapp.ui.movie

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diegobezerra.cinemaisapp.util.setValueIfNew
import com.diegobezerra.core.cinemais.data.movie.MoviesRepository
import com.diegobezerra.core.cinemais.domain.model.Movie
import com.diegobezerra.core.util.RxUtils
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class MovieViewModel @Inject constructor(
    private val moviesRepository: MoviesRepository
) : ViewModel() {

    private val _loading1 = MutableLiveData<Boolean>()
    val loading1: LiveData<Boolean>
        get() = _loading1

    private val _loading2 = MutableLiveData<Boolean>()
    val loading2: LiveData<Boolean>
        get() = _loading2

    private val _movie = MediatorLiveData<Movie>()
    val movie: LiveData<Movie>
        get() = _movie

    private val movieId = MutableLiveData<Int>()

    private val disposables = CompositeDisposable()

    init {
        _movie.addSource(movieId) {
            refreshMovie()
        }
    }

    override fun onCleared() {
        disposables.clear()
    }

    fun refresh() {
        refreshMovie(true)
    }

    private fun refreshMovie(remote: Boolean = false) {
        getMovieId()?.let {
            if (remote) {
                moviesRepository.clearMovieWithId(it)
            }
            disposables.add(RxUtils.getSingle(moviesRepository.getMovie(it))
                .doOnSubscribe {
                    setLoading(true)
                }
                .doOnSuccess {
                    setLoading(false)
                }
                .doOnError { throwable ->
                    setLoading(false)
                    // TODO: Implement error handling
                    Timber.d("throwable=$throwable")
                }
                .subscribe({ movie ->
                    _movie.value = movie
                }, { throwable ->
                    Timber.d("throwable=$throwable")
                })
            )
        }
    }

    private fun isFirstLoad(): Boolean {
        return _movie.value == null
    }

    private fun setLoading(value: Boolean) {
        if (isFirstLoad()) {
            _loading1.value = value
        } else {
            _loading2.value = value
        }
    }

    /**
     * Sets the current movie ID only if it's new.
     */
    fun setMovieId(newMovieId: Int) {
        movieId.setValueIfNew(newMovieId)
    }

    /**
     * Returns the current movie ID or null if not available.
     */
    private fun getMovieId(): Int? = movieId.value
}
