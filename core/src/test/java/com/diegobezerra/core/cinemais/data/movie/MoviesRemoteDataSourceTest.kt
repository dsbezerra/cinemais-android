package com.diegobezerra.core.cinemais.data.movie

import com.diegobezerra.core.cinemais.data.CinemaisService
import com.diegobezerra.core.cinemais.data.movie
import com.diegobezerra.core.cinemais.data.movie.remote.MoviesRemoteDataSource
import com.diegobezerra.core.cinemais.domain.model.Movie
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Test

class MovieRemoteDataSourceTest {

    private val service: CinemaisService = mock()
    private val dataSource = MoviesRemoteDataSource(service)

    private val id = 11064

    @Test
    fun movie_whenRequestSuccessful() {
        val testObserver = TestObserver<Movie>()

        // Given that the service responds with success
        val result = Single.just(movie)
        whenever(service.movie(id))
            .thenReturn(result)

        // When performing a get movie
        dataSource.getMovie(id).subscribe(testObserver)

        // Then the response is as expected
        testObserver.assertNoErrors()
        testObserver.assertResult(movie)
    }
}
