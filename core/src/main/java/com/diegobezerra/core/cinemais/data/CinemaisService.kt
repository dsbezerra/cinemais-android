package com.diegobezerra.core.cinemais.data

import com.diegobezerra.core.cinemais.domain.model.Cinema
import com.diegobezerra.core.cinemais.domain.model.Home
import com.diegobezerra.core.cinemais.domain.model.Images
import com.diegobezerra.core.cinemais.domain.model.Location
import com.diegobezerra.core.cinemais.domain.model.Movie
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.cinemais.domain.model.Tickets
import com.diegobezerra.core.cinemais.domain.model.Trailer
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
    suspend fun home(): Home

    @CinemaisNowPlaying
    @GET("programacao")
    suspend fun playing(): List<Movie>

    @CinemaisUpcoming
    @GET("proximos_lancamentos")
    suspend fun upcoming(): List<Movie>

    @CinemaisCinemas
    @GET("/")
    suspend fun cinemas(): List<Cinema>

    @GET("programacao/cinema.php")
    suspend fun schedule(
        @Query("cc") id: Int
    ): Schedule

    @GET("programacao/proxima_semana.php")
    suspend fun nextWeekSchedule(
        @Query("cc") id: Int,
        @Query("d") date: String
    ): Schedule

    @GET("programacao/mapa.php")
    suspend fun location(
        @Query("cc") id: Int
    ): Location

    @GET("programacao/ingresso_velox.php")
    suspend fun tickets(
        @Query("cc") id: Int
    ): Tickets

    @GET("filmes/filme.php")
    suspend fun movie(
        @Query("cf") id: Int
    ): Movie

    @GET("filmes/trailer.php")
    suspend fun trailer(
        @Query("ct") id: String,
        @Query("tam") tam: Int = 2 // Defaults to 2
    ): Trailer

    @GET("filmes/fotos.php")
    suspend fun images(
        @Query("cf") movieId: Int
    ): Images
}