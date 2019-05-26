package com.diegobezerra.core.cinemais.data.movie.remote

import com.diegobezerra.core.cinemais.data.CinemaisService
import com.diegobezerra.core.cinemais.data.movie.MoviesDataSource
import com.diegobezerra.core.cinemais.domain.model.Movie
import com.diegobezerra.core.cinemais.domain.model.Trailer
import com.diegobezerra.core.cinemais.domain.model.Trailer.Companion.SOURCE_CINEMAIS
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class MoviesRemoteDataSource @Inject constructor(
    private val service: CinemaisService
) : MoviesDataSource {

    override fun getNowPlaying(): Single<List<Movie>> = service.playing()

    override fun getUpcoming(): Single<List<Movie>> = service.upcoming()

    override fun getMovie(id: Int): Single<Movie> {
        return service.movie(id)
            .flatMap { movie ->
                service.images(id).map {
                    movie.images = it
                    movie
                }
            }
            .flatMap { movie ->
                if (movie.trailer == null || movie.trailer!!.isYoutube()) {
                    Single.just(movie)
                } else {
                    service.trailer(movie.trailer!!.id).map {
                        movie.trailer = it
                        movie
                    }
                }
            }
    }
}