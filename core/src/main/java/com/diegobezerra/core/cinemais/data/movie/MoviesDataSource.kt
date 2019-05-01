package com.diegobezerra.core.cinemais.data.movie

import com.diegobezerra.core.cinemais.domain.model.Movie
import io.reactivex.Single

interface MoviesDataSource {
    fun getNowPlaying(): Single<List<Movie>>
    fun getUpcoming(): Single<List<Movie>>
    fun getMovie(id: Int): Single<Movie>
}