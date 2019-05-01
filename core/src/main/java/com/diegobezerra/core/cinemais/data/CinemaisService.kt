package com.diegobezerra.core.cinemais.data

import com.diegobezerra.core.cinemais.domain.model.Cinema
import com.diegobezerra.core.cinemais.domain.model.HomeData
import com.diegobezerra.core.cinemais.domain.model.Images
import com.diegobezerra.core.cinemais.domain.model.Location
import com.diegobezerra.core.cinemais.domain.model.Movie
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.cinemais.domain.model.Tickets
import com.diegobezerra.core.cinemais.domain.model.Trailer
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Used to differentiate Now Playing page response from others.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class CinemaisNowPlaying

/**
 * Used to differentiate Upcoming page response from others.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class CinemaisUpcoming

/**
 * Used to differentiate Cinemas page response from others.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class CinemaisCinemas

/**
 * Cinemais "API" created by scraping the website
 */
interface CinemaisService {

    companion object {

        const val ENDPOINT = "http://cinemais.com.br/"
    }

    @GET("/")
    fun home(): Single<HomeData>

    @CinemaisNowPlaying
    @GET("programacao")
    fun playing(): Single<List<Movie>>

    @CinemaisUpcoming
    @GET("proximos_lancamentos")
    fun upcoming(): Single<List<Movie>>

    @CinemaisCinemas
    @GET("/")
    fun cinemas(): Single<List<Cinema>>

    @GET("programacao/cinema.php")
    fun cinemaSchedule(
        @Query("cc") id: Int
    ): Single<Schedule>

    @GET("programacao/mapa.php")
    fun cinemaLocation(
        @Query("cc") id: Int
    ): Single<Location>

    @GET("programacao/ingresso_velox.php")
    fun cinemaTickets(
        @Query("cc") id: Int
    ): Single<Tickets>

    @GET("filmes/filme.php")
    fun movie(
        @Query("cf") id: Int
    ): Single<Movie>

    @GET("filmes/trailer.php")
    fun trailer(
        @Query("ct") id: String,
        @Query("tam") tam: Int = 2 // Defaults to 2
    ): Single<Trailer>

    @GET("filmes/fotos.php")
    fun images(
        @Query("cf") movieId: Int
    ): Single<Images>
}