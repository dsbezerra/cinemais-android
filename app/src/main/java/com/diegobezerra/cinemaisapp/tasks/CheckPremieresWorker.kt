package com.diegobezerra.cinemaisapp.tasks

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.diegobezerra.cinemaisapp.GlideApp
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.dagger.DaggerWorkerFactory
import com.diegobezerra.cinemaisapp.data.local.PreferencesHelper
import com.diegobezerra.cinemaisapp.ui.main.MainActivity
import com.diegobezerra.cinemaisapp.ui.movie.MovieActivity
import com.diegobezerra.core.cinemais.data.cinemas.CinemaRepository
import com.diegobezerra.core.cinemais.data.movie.MovieRepository
import com.diegobezerra.core.cinemais.domain.model.Cinema
import com.diegobezerra.core.cinemais.domain.model.Movie
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.util.DateUtils
import io.karn.notify.Notify
import io.karn.notify.internal.utils.Action
import timber.log.Timber
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CheckPremieresWorker constructor(
    private val preferencesHelper: PreferencesHelper,
    private val cinemaRepository: CinemaRepository,
    private val movieRepository: MovieRepository,
    private val context: Context,
    private val workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val NAME = "check_premieres_worker"
        private const val ONE_PREMIERE_REQUEST_CODE = 4000

        private const val START_HOUR = 7
        private val TRY_AGAIN_DELAY = TimeUnit.HOURS.toMillis(3)

        @JvmStatic
        fun scheduleToNextThursday(context: Context) {
            val currentPlayingRange = DateUtils.playingRange(null)
            val nextThursday = Calendar.getInstance().apply {
                timeInMillis = currentPlayingRange.end.time
                add(Calendar.DATE, 1)
                add(Calendar.HOUR, START_HOUR)
            }
            scheduleWithDelay(
                context,
                nextThursday.timeInMillis - Calendar.getInstance().timeInMillis
            )
        }

        @JvmStatic
        fun scheduleWithDelay(context: Context, delay: Long) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                NAME, ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<CheckPremieresWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .build()
            )
        }
    }

    override suspend fun doWork(): Result {
        val now = Calendar.getInstance().time
        Timber.d("Checking premieres... %d:%d", now.hours, now.minutes)

        // Get favorite cinema and notify premieres
        preferencesHelper.getSelectedCinemaId()?.let { cinemaId ->
            getCinemaAndMovies(cinemaId,
                onSuccess = { cinema, movies ->
                    notifyPremieres(cinema, movies)
                    scheduleToNextThursday(context)
                },
                onError = { scheduleWithDelay(context, TRY_AGAIN_DELAY) })
        }
        return Result.success()
    }

    private suspend fun getCinemaAndMovies(
        id: Int,
        onSuccess: (cinema: Cinema, movies: List<Movie>) -> Unit,
        onError: () -> Unit
    ) {
        return try {
            // Get cinema and movies from schedule
            val schedule = getSchedule(id)
            val movies = schedule.sessions.distinctBy { it.movieId }
                .map { getMovieInfo(it.movieId) }
                .filter { it.releaseDate != null && DateUtils.isToday(it.releaseDate!!.time) }
            onSuccess(schedule.cinema, movies)
        } catch (e: Exception) {
            Timber.e(e)
            onError()
        }
    }

    private suspend fun getSchedule(id: Int): Schedule {
        return getOrThrow(
            call = {
                cinemaRepository.clearSchedule(id)
                cinemaRepository.getSchedule(id)
            }
        )
    }

    private suspend fun getMovieInfo(id: Int): Movie {
        return getOrThrow(call = { movieRepository.getMovie(id) })
    }

    private suspend fun <T : Any> getOrThrow(call: suspend () -> com.diegobezerra.core.result.Result<T>): T {
        return when (val result = call()) {
            is com.diegobezerra.core.result.Result.Success -> result.data
            is com.diegobezerra.core.result.Result.Error -> throw result.exception
        }
    }

    private fun notifyPremieres(cinema: Cinema, movies: List<Movie>) {
        when (movies.size) {
            1 -> notifyOne(cinema, movies[0])
            else -> notifyMany(cinema, movies)
        }
    }

    private fun notifyMany(
        cinema: Cinema,
        movies: List<Movie>
    ) {
        if (movies.isEmpty()) {
            Timber.d("There's no movies to notify.")
            return
        }

        Timber.d("Notify for %d premieres... ${movies.size}")
        Notify.with(context)
            .header {
                icon = R.drawable.ic_stat_notification
            }
            .meta {
                clickIntent = PendingIntent.getActivity(
                    context,
                    ONE_PREMIERE_REQUEST_CODE,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            .asBigText {
                title = context.resources.getQuantityString(
                    R.plurals.notification_title_premieres,
                    movies.size
                )
                val secondLine = context.getString(R.string.cinemais_prefixed_name, cinema.name)
                text = secondLine
                expandedText = secondLine
                bigText = movies.joinToString(separator = "\n", transform = { it.title })
            }
            .show()
    }

    private fun notifyOne(cinema: Cinema, movie: Movie) {
        Timber.d("Notify for premiere ${movie.title}...")
        Notify.with(context)
            .header {
                icon = R.drawable.ic_stat_notification
            }
            .meta {
                clickIntent = PendingIntent.getActivity(
                    context,
                    ONE_PREMIERE_REQUEST_CODE,
                    MovieActivity.getStartIntent(context, movie.id),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            .asBigPicture {
                val weekday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                this.image = GlideApp.with(context)
                    .asBitmap()
                    // Retrieve backdrop based on weekday and if we can't find one fallback to poster
                    .load(movie.images.getBackdrop(weekday) ?: movie.posters.large)
                    .submit()
                    .get()
                title = context.getString(R.string.notification_channel_premiere)
                text = context.getString(R.string.cinemais_prefixed_name, cinema.name)
                expandedText = movie.title
            }
            .actions {
                add(
                    Action(
                        R.drawable.ic_movie,
                        context.getString(R.string.notification_action_movie_details),
                        PendingIntent.getActivity(
                            context,
                            movie.id,
                            MovieActivity.getStartIntent(context, movie.id),
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )
                )
                movie.trailer?.let { trailer ->
                    if (trailer.isYoutube()) {
                        add(getWatchTrailerAction(movie.id, trailer.id))
                    }
                }
            }
            .show()
    }

    private fun getWatchTrailerAction(id: Int, trailerId: String): Action {
        return Action(
            R.drawable.ic_play_arrow,
            context.getString(R.string.trailer),
            PendingIntent.getActivity(
                context,
                id,
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://youtube.com/watch?v=$trailerId")
                ),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
    }

    class Factory @Inject constructor(
        private val preferencesHelper: PreferencesHelper,
        private val cinemaRepository: CinemaRepository,
        private val movieRepository: MovieRepository
    ) : DaggerWorkerFactory.ChildWorkerFactory {
        override fun create(appContext: Context, params: WorkerParameters): ListenableWorker =
            CheckPremieresWorker(
                preferencesHelper,
                cinemaRepository,
                movieRepository,
                appContext,
                params
            )
    }
}