package com.diegobezerra.cinemaisapp.ui.movie

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.diegobezerra.cinemaisapp.base.BaseViewModel
import com.diegobezerra.cinemaisapp.util.setValueIfNew
import com.diegobezerra.core.cinemais.data.movie.MovieRepository
import com.diegobezerra.core.cinemais.domain.model.Movie
import javax.inject.Inject

class MovieViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : BaseViewModel() {

    private val _movie = MediatorLiveData<Movie>()
    val movie: LiveData<Movie>
        get() = _movie

    private val movieId = MutableLiveData<Int>()

    init {
        _movie.addSource(movieId) {
            refreshMovie()
        }
    }

    fun refresh() {
        refreshMovie(true)
    }

    private fun refreshMovie(ignoreCache: Boolean = false) {
        getMovieId()?.let { movieId ->
            if (ignoreCache) {
                movieRepository.clearMovieWithId(movieId)
            }
            execute(
                { movieRepository.getMovie(movieId) },
                onSuccess = {
                    _movie.value = it
                },
                onError = {
                    // No-op
                })
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
