package com.diegobezerra.core.cinemais.data.movie

import com.diegobezerra.core.cinemais.data.CinemaisService
import com.diegobezerra.core.cinemais.data.movie
import com.diegobezerra.core.cinemais.data.movie.remote.MovieRemoteDataSource
import com.diegobezerra.shared.result.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MovieRemoteDataSourceTest {

    private val service: CinemaisService = mock()
    private val dataSource = MovieRemoteDataSource(service)
    private val id = 11064

    @Test
    fun movie_whenRequestSuccessful() = runBlocking {
        // Given that the service responds with success
        val result = Result.Success(movie)
        whenever(service.movie(id)).thenReturn(movie)

        val movie = dataSource.getMovie(id)
        assertNotNull(movie)
        assertEquals(movie, result)
    }
}
