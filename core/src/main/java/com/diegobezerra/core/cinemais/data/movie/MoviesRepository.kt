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

    private val cachedLists: Array<List<Movie>?> = arrayOf(null, null)

    private val cachedMovies: MutableMap<Int, Movie> = hashMapOf()

    fun getNowPlaying(): Single<List<Movie>> {
        val cached = cachedLists[NOW_PLAYING]
        return if (cached != null) {
            Single.just(cached)
        } else {
            remoteDataSource.getNowPlaying()
                .doOnSuccess {
                    cachedLists[NOW_PLAYING] = it
                }
        }
    }

    fun getUpcoming(): Single<List<Movie>> {
        val cached = cachedLists[UPCOMING]
        return if (cached != null) {
            Single.just(cached)
        } else {
            remoteDataSource.getUpcoming()
                .doOnSuccess {
                    cachedLists[UPCOMING] = it
                }
        }
    }

    fun getMovie(id: Int): Single<Movie> {
        val cached = cachedMovies[id]
        return if (cached != null) {
            Single.just(cached)
        } else {
            remoteDataSource.getMovie(id)
                .doOnSuccess {
                    cachedMovies[id] = it
                }
        }
    }

    fun getPlayingCinemas(movieId: Int): Single<List<Cinema>> {
        return getMovie(movieId)
            .map {
                it.playingCinemas
            }
    }

    fun clearMovieWithId(movieId: Int) {
        cachedMovies.remove(movieId)
    }

    fun clearMovies(index: Int) {
        if (index >= 0 && index < cachedLists.size) {
            cachedLists[index] = null
        }
    }
}