package com.diegobezerra.core.cinemais.data.movie

import com.diegobezerra.core.cinemais.domain.model.Movie
import com.diegobezerra.core.result.Result

interface MovieDataSource {

    suspend fun getMovie(id: Int): Result<Movie>

    suspend fun getNowPlaying(): Result<List<Movie>>

    suspend fun getUpcoming(): Result<List<Movie>>
}