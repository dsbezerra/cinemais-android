package com.diegobezerra.cinemaisapp.tasks

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType.UNMETERED
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

        private const val START_HOUR = 7 // Check start hour is 7h
        // If the check at 7h retrieves zero premieres, try again at 12h
        private const val SECOND_START_HOUR = 12

        private const val MAX_ATTEMPTS = 3

        @JvmStatic
        fun scheduleToNextThursday(context: Context, hour: Int = START_HOUR) {
            val currentPlayingRange = DateUtils.playingRange(null)
            val nextThursday = Calendar.getInstance().apply {
                timeInMillis = currentPlayingRange.end.time
                add(Calendar.DATE, 1)
                add(Calendar.HOUR, hour)
            }
            scheduleWithDelay(
                context,
                nextThursday.timeInMillis - Calendar.getInstance().timeInMillis
            )
        }

        @JvmStatic
        fun scheduleWithDelay(context: Context, delay: Long) {
            val constraints = Constraints.Builder()
                // TODO: Add settings to choose between Wi-Fi or Mobile
                .setRequiredNetworkType(UNMETERED)
                .setRequiresBatteryNotLow(true)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                NAME, ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<CheckPremieresWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setConstraints(constraints)
                    .build()
            )
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val now = Calendar.getInstance().time
        Timber.d("Checking premieres... %d:%d", now.hours, now.minutes)
        var retry = false
        try {
            if (runAttemptCount > MAX_ATTEMPTS) {
                Result.failure()
            } else {
                // Get favorite cinema and notify premieres
                val cinemaId = preferencesHelper.getSelectedCinemaId()
                if (cinemaId == null) {
                    // We don't have a selected cinema let's schedule to next
                    // thursday and hope we will have one
                    Result.failure()
                } else {
                    getCinemaAndMovies(cinemaId).let {
                        // NOTE(diego): Sometimes the schedule page will be blank until midday, so
                        // if the first check returns a blank schedule, reschedule the work to run
                        // at SECOND_START_HOUR.
                        //
                        // Third value means the schedule has zero sessions.
                        if (it.third) {
                            scheduleToNextThursday(context, SECOND_START_HOUR)
                            // And to avoid rescheduling to next Thursday (at START_HOUR) inside the
                            // finally scope we set retry to true
                            retry = true
                        } else {
                            notifyPremieres(it.first, it.second)
                        }
                        Result.success()
                    }
                }
            }
        } catch (e: Exception) {
            // Ops... something wrong happened. Try again later.
            retry = true
            Timber.e(e)
            Result.retry()
        } finally {
            if (!retry) {
                scheduleToNextThursday(context)
            }
        }
    }

    private suspend fun getCinemaAndMovies(id: Int): Triple<Cinema, List<Movie>, Boolean> {
        // Get cinema and movies from schedule
        val schedule = getSchedule(id)
        val movies = schedule.sessions.distinctBy { it.movieId }
            .map { getMovieInfo(it.movieId) }
            .filter { it.releaseDate != null && DateUtils.isToday(it.releaseDate!!.time) }
        return Triple(schedule.cinema, movies, schedule.sessions.isEmpty())
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
                title =
                    context.resources.getQuantityString(R.plurals.notification_title_premieres, 1)
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