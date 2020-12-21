package com.diegobezerra.core.cinemais.data.movie.remote

import com.diegobezerra.core.cinemais.data.CinemaisService
import com.diegobezerra.core.cinemais.data.movie.MovieDataSource
import com.diegobezerra.core.cinemais.domain.model.Images
import com.diegobezerra.core.cinemais.domain.model.Movie
import com.diegobezerra.core.cinemais.domain.model.Trailer
import com.diegobezerra.core.util.safeRequest
import com.diegobezerra.shared.result.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import javax.inject.Inject

class MovieRemoteDataSource @Inject constructor(
    private val service: CinemaisService
) : MovieDataSource {

    override suspend fun getMovie(id: Int) =
        requestMovieWithImagesAndTrailer(id)

    override suspend fun getNowPlaying(): Result<List<Movie>> = safeRequest(
        call = { service.playing() }
    )

    override suspend fun getUpcoming(): Result<List<Movie>> = safeRequest(
        call = { service.upcoming() }
    )

    private suspend fun requestMovieWithImagesAndTrailer(id: Int): Result<Movie> {
        val result = safeRequest(call = { service.movie(id) })
        if (result is Result.Success) {
            requestImagesAndTrailer(result.data)
        }
        return result
    }

    private suspend fun requestImages(id: Int): Result<Images> =
        safeRequest(call = { service.images(id) })

    private suspend fun requestTrailer(id: String): Result<Trailer> =
        safeRequest(call = { service.trailer(id) })

    private suspend fun requestImagesAndTrailer(movie: Movie) =
        coroutineScope {
            val trailerId = if (movie.trailer != null && !movie.trailer!!.isYoutube()) {
                movie.trailer!!.id
            } else {
                ""
            }
            val deferreds = listOf(
                async {
                    requestImages(movie.id).let {
                        if (it is Result.Success) {
                            movie.images = it.data
                        }
                    }
                },
                async {
                    requestTrailer(trailerId).let {
                        if (it is Result.Success) {
                            movie.trailer = it.data
                        }
                    }
                }
            )
            deferreds.awaitAll()
        }
}