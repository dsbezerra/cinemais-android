package com.diegobezerra.cinemaisapp.ui.main.movies

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.diegobezerra.cinemaisapp.base.BaseViewModel
import com.diegobezerra.cinemaisapp.util.setValueIfNew
import com.diegobezerra.core.cinemais.data.movie.MovieRepository
import com.diegobezerra.core.cinemais.data.movie.MovieRepository.Companion.NOW_PLAYING
import com.diegobezerra.core.cinemais.domain.model.Movie
import com.diegobezerra.core.event.Event
import com.diegobezerra.core.result.Result
import javax.inject.Inject

class TabMoviesViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : BaseViewModel(), MoviesEventListener {

    private var _type = MutableLiveData<Int>()

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

    fun refresh() {
        loadMovies(true)
    }

    private fun loadMovies(ignoreCache: Boolean = false) {
        getType()?.let { type ->
            if (ignoreCache) {
                movieRepository.clearMovies(type)
            }
            execute({ getCallForType(type) },
                onSuccess = {
                    _movies.value = it
                },
                onError = {
                    // No-op.
                })
        }
    }

    override fun onMovieClicked(movieId: Int) {
        _navigateToMovieDetail.postValue(Event(movieId))
    }

    private fun getType(): Int? {
        return _type.value
    }

    fun setType(type: Int) {
        _type.setValueIfNew(type)
    }

    private suspend fun getCallForType(type: Int): Result<List<Movie>> {
        return if (type == NOW_PLAYING) {
            movieRepository.getNowPlaying()
        } else {
            movieRepository.getUpcoming()
        }
    }
}

interface MoviesEventListener {

    fun onMovieClicked(movieId: Int)

}