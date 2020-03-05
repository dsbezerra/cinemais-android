package com.diegobezerra.cinemaisapp.fcm

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.diegobezerra.cinemaisapp.GlideApp
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.ui.main.MainActivity
import com.diegobezerra.cinemaisapp.ui.movie.MovieActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.karn.notify.Notify
import io.karn.notify.internal.utils.Action
import org.json.JSONArray
import java.net.SocketTimeoutException

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val RELEASE_REQUEST_CODE = 4000
        private const val RELEASES_REQUEST_CODE = 4001

        const val KEY_TYPE = "type"
        const val KEY_MOVIE_ID = "movie_id"
        const val KEY_THEATER_ID = "theater_id"
        const val KEY_IMAGE = "image"
        const val KEY_TEXT = "text"
        const val KEY_EXPANDED_TEXT = "expanded_text"
        const val KEY_BIG_TEXT = "big_text"
        const val KEY_ACTIONS = "actions"
        const val KEY_VIDEO_ID = "video_id"

        const val TYPE_RELEASE = "release"
        const val TYPE_RELEASES = "releases"

        const val ACTION_MOVIE_DETAILS = "movie_details"
        const val ACTION_VIEW_TRAILER = "view_trailer"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        val context = applicationContext
        when (data[KEY_TYPE]) {
            TYPE_RELEASE -> notifyRelease(context, data)
            TYPE_RELEASES -> notifyReleases(context, data)
        }
    }

    private fun notifyRelease(context: Context, data: Map<String, String>) {
        val requiredKeys = listOf(
            KEY_TEXT,
            KEY_EXPANDED_TEXT,
            KEY_IMAGE,
            KEY_MOVIE_ID
        )

        if (!isPayloadValid(data, requiredKeys)) {
            return
        }

        val movieId = data[KEY_MOVIE_ID]?.toInt() ?: 0
        if (movieId == 0) {
            return
        }

        val creator = Notify.with(context)
            .header {
                icon = R.drawable.ic_stat_notification
            }
            .meta {
                clickIntent = PendingIntent.getActivity(
                    context,
                    RELEASE_REQUEST_CODE,
                    MovieActivity.getStartIntent(context, movieId),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            .actions {
                val actionsJson = JSONArray(data[KEY_ACTIONS])
                val length = actionsJson.length()
                for (i in 0 until length) {
                    val obj = actionsJson.getJSONObject(i)
                    when (obj.getString(KEY_TYPE)) {
                        ACTION_MOVIE_DETAILS -> add(
                            getMovieDetailsAction(context, movieId)
                        )
                        ACTION_VIEW_TRAILER -> add(
                            getViewTrailerAction(context, movieId, obj.getString(KEY_VIDEO_ID))
                        )
                    }
                }
            }

        try {
            creator.asBigPicture {
                image = GlideApp.with(context)
                    .asBitmap()
                    .load(data[KEY_IMAGE])
                    .submit()
                    .get()
                title =
                    context.resources.getQuantityString(R.plurals.notification_title_premieres, 1)
                text = data[KEY_TEXT]
                expandedText = data[KEY_EXPANDED_TEXT]
            }.show()
        } catch (e: SocketTimeoutException) {
            // Ignore poster
            creator.content {
                title =
                    context.resources.getQuantityString(R.plurals.notification_title_premieres, 1)
                text = data[KEY_TEXT]
            }.show()
        } catch (e: Exception) {
            // No-op
        }
    }

    private fun notifyReleases(context: Context, data: Map<String, String>) {
        val requiredKeys = listOf(
            KEY_TEXT,
            KEY_EXPANDED_TEXT,
            KEY_BIG_TEXT,
            KEY_THEATER_ID
        )

        if (!isPayloadValid(data, requiredKeys)) {
            return
        }

        Notify.with(context)
            .header {
                icon = R.drawable.ic_stat_notification
            }
            .meta {
                clickIntent = PendingIntent.getActivity(
                    context,
                    RELEASES_REQUEST_CODE,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            .asBigText {
                title = context.resources.getQuantityString(
                    R.plurals.notification_title_premieres,
                    2
                )
                text = data[KEY_TEXT]
                expandedText = data[KEY_EXPANDED_TEXT]
                bigText = data[KEY_BIG_TEXT]
            }
            .show()
    }

    private fun isPayloadValid(payload: Map<String, String>, requiredKeys: List<String>): Boolean {
        requiredKeys.forEach {
            if (!payload.containsKey(it)) {
                return false
            }
        }
        return true
    }

    private fun getMovieDetailsAction(context: Context, movieId: Int): Action {
        return Action(
            R.drawable.ic_movie,
            context.getString(R.string.notification_action_movie_details),
            PendingIntent.getActivity(
                context,
                movieId,
                MovieActivity.getStartIntent(context, movieId),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
    }

    private fun getViewTrailerAction(context: Context, id: Int, videoId: String): Action {
        return Action(
            R.drawable.ic_play_arrow,
            context.getString(R.string.trailer),
            PendingIntent.getActivity(
                context,
                id,
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://youtube.com/watch?v=$videoId")
                ),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
    }
}
