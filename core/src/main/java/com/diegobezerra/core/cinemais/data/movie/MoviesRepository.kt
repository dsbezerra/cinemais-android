package com.diegobezerra.core.cinemais.data.movie

import com.diegobezerra.core.cinemais.data.movie.remote.MoviesRemoteDataSource
import com.diegobezerra.core.cinemais.domain.model.Cinema
import com.diegobezerra.core.cinemais.domain.model.Movie
import io.reactivex.Single
import javax.inject.Inject

class MoviesRepository @Inject constructor(
    private val remoteDataSource: MoviesRemoteDataSource
) {

    companion object {
        const val NOW_PLAYING = 0
        const val UPCOMING = 1
    }

    private var playingMovies: List<Movie>? = null
    private var upcomingMovies: List<Movie>? = null

    // Simple cache to avoid requesting the same movie again and again
    private val cachedMovies: MutableMap<Int, Movie> = hashMapOf()

    fun getNowPlaying(): Single<List<Movie>> {
        return if (playingMovies != null) {
            Single.just(playingMovies)
        } else {
            remoteDataSource.getNowPlaying()
                .doOnSuccess { playingMovies = it }
        }
    }

    fun getUpcoming(): Single<List<Movie>> {
        return if (upcomingMovies != null) {
            Single.just(upcomingMovies)
        } else {
            remoteDataSource.getUpcoming()
                .doOnSuccess { upcomingMovies = it }
        }
    }

    fun getMovie(id: Int): Single<Movie> {
        val cached = cachedMovies[id]
        return if (cached != null) {
            Single.just(cached)
        } else {
            remoteDataSource.getMovie(id)
                .doOnSuccess { cachedMovies[id] = it }
        }
    }

    fun getPlayingCinemas(movieId: Int): Single<List<Cinema>>
        = getMovie(movieId).map { it.playingCinemas }

    private fun clearPlayingMovies() {
        playingMovies = null
    }

    private fun clearUpcomingMovies() {
        upcomingMovies = null
    }

    fun clearMovieWithId(id: Int) {
        cachedMovies.remove(id)
    }

    fun clearMovies(type: Int) {
        when (type) {
            NOW_PLAYING -> clearPlayingMovies()
            else -> clearUpcomingMovies()
        }
    }
}