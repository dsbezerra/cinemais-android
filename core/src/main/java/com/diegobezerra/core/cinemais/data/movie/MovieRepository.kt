package com.diegobezerra.core.cinemais.data.movie

import com.diegobezerra.core.cinemais.data.movie.remote.MovieRemoteDataSource
import com.diegobezerra.core.cinemais.domain.model.Cinema
import com.diegobezerra.core.cinemais.domain.model.Movie
import com.diegobezerra.shared.result.Result
import com.diegobezerra.shared.result.getRemoteAndCache
import javax.inject.Inject

class MovieRepository @Inject constructor(
    private val remoteDataSource: MovieRemoteDataSource
) {

    companion object {
        const val NOW_PLAYING = 0
        const val UPCOMING = 1
    }

    private val listCache: Array<List<Movie>?> = arrayOf(null, null)
    private val movieCache: HashMap<Int, Movie> = hashMapOf()

    suspend fun getNowPlaying(): Result<List<Movie>> {
        val cached = listCache[NOW_PLAYING]
        return if (cached != null) {
            Result.Success(cached)
        } else {
            val result = remoteDataSource.getNowPlaying()
            if (result is Result.Success) {
                listCache[NOW_PLAYING] = result.data
            }
            result
        }
    }

    suspend fun getUpcoming(): Result<List<Movie>> {
        val cached = listCache[UPCOMING]
        return if (cached != null) {
            Result.Success(cached)
        } else {
            val result = remoteDataSource.getUpcoming()
            if (result is Result.Success) {
                listCache[UPCOMING] = result.data
            }
            result
        }
    }

    suspend fun getMovie(id: Int): Result<Movie> {
        val cached = movieCache[id]
        return if (cached != null) {
            Result.Success(cached)
        } else {
            getRemoteAndCache(
                call = { remoteDataSource.getMovie(id) },
                cacheMap = movieCache,
                entryKey = id
            )
        }
    }

    suspend fun getPlayingCinemas(movieId: Int): Result<List<Cinema>> {
        val result = getMovie(movieId)
        if (result is Result.Success) {
            return Result.Success(result.data.playingCinemas)
        }
        return result as Result.Error
    }

    fun clearMovieWithId(movieId: Int) {
        movieCache.remove(movieId)
    }

    fun clearMovies(index: Int) {
        if (index >= 0 && index < listCache.size) {
            listCache[index] = null
        }
    }
}