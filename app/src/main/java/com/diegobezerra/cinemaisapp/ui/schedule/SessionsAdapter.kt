package com.diegobezerra.cinemaisapp.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.ui.movie.MovieActivity
import com.diegobezerra.cinemaisapp.ui.schedule.SessionViewHolder.MovieTitleViewHolder
import com.diegobezerra.cinemaisapp.ui.schedule.SessionViewHolder.SessionsViewHolder
import com.diegobezerra.cinemaisapp.util.groups
import com.diegobezerra.core.cinemais.domain.model.Session
import com.diegobezerra.core.cinemais.domain.model.Session.Companion.VersionDubbed
import com.diegobezerra.core.cinemais.domain.model.Session.Companion.VersionNational
import com.diegobezerra.core.cinemais.domain.model.Session.Companion.VersionSubtitled
import com.diegobezerra.core.cinemais.domain.model.Session.Companion.VideoFormat3D

class SessionsAdapter : RecyclerView.Adapter<SessionViewHolder>() {

    companion object {

        const val VIEW_TYPE_MOVIE = 0
        const val VIEW_TYPE_SESSIONS = 1

    }

    private var list: MutableList<Any> = mutableListOf()

    var data: List<Session> = emptyList()
        set(value) {
            field = value
            buildList()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_MOVIE -> MovieTitleViewHolder(
                inflater.inflate(R.layout.item_movie_title, parent, false)
            )
            VIEW_TYPE_SESSIONS -> SessionsViewHolder(
                inflater.inflate(R.layout.item_session_group, parent, false)
            )
            else -> throw IllegalStateException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        when (holder) {
            is MovieTitleViewHolder -> holder.apply {
                (list[position] as MovieMinimal).let { movie ->
                    title.text = movie.title
                    title.setOnClickListener {
                        it.context.startActivity(MovieActivity.getStartIntent(it.context, movie.id))
                    }
                    val drawableRes = when (movie.rating) {
                        -1 -> R.drawable.ic_rating_l
                        10 -> R.drawable.ic_rating_10
                        12 -> R.drawable.ic_rating_12
                        14 -> R.drawable.ic_rating_14
                        16 -> R.drawable.ic_rating_16
                        18 -> R.drawable.ic_rating_18
                        else -> 0
                    }
                    if (drawableRes != 0) {
                        with(itemView.context) {
                            ContextCompat.getDrawable(this, drawableRes)?.let {
                                val size =
                                    resources.getDimension(R.dimen.size_compound_rating).toInt()
                                it.setBounds(0, 0, size, size)
                                title.setCompoundDrawables(null, null, it, null)
                                title.compoundDrawablePadding =
                                    resources.getDimension(R.dimen.spacing_small).toInt()
                            }

                        }
                    }
                }
            }
            is SessionsViewHolder -> holder.apply {
                val sessions = list[position] as SessionGroup
                val context = itemView.context

                room.text = context.getString(R.string.label_session_room, sessions.room)
                when (sessions.version) {
                    VersionNational -> {
                        version.text = context.getString(R.string.label_session_national)
                    }
                    VersionDubbed -> {
                        version.text = context.getString(R.string.label_session_dubbed)
                    }
                    VersionSubtitled -> {
                        version.text = context.getString(R.string.label_session_subtitled)
                    }
                }
                vip.isGone = !sessions.vip
                magic.isGone = !sessions.magic
                format.isGone = sessions.format != VideoFormat3D

                // TODO: Replace this TextView with a RecyclerView once we integrate with VeloxTickets
                times.text = sessions.sessions.joinToString(", ") { it.startTime }

            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (list[position]) {
            is MovieMinimal -> VIEW_TYPE_MOVIE
            is SessionGroup -> VIEW_TYPE_SESSIONS
            else -> throw IllegalStateException("Unknown view type at position $position")
        }
    }

    override fun getItemCount() = list.size

    private fun buildList() {
        list.clear()

        if (data.isEmpty()) return

        val result = mutableListOf<Any>()
        // Used just to keep track of inserted titles
        val movieIdMap = hashMapOf<Int, Unit>()
        data.groups().forEach { group ->
            if (!movieIdMap.containsKey(group.movieId)) {
                result += MovieMinimal(
                    group.movieId,
                    group.movieTitle,
                    group.movieRating
                )
                movieIdMap[group.movieId] = Unit
            }
            result += group
        }

        list = result
    }

}

sealed class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    class MovieTitleViewHolder(itemView: View) : SessionViewHolder(itemView) {

        val title: TextView = itemView.findViewById(R.id.title)

    }

    class SessionsViewHolder(itemView: View) : SessionViewHolder(itemView) {

        val room: TextView = itemView.findViewById(R.id.room)
        val version: TextView = itemView.findViewById(R.id.version)
        val format: TextView = itemView.findViewById(R.id.format)
        val magic: TextView = itemView.findViewById(R.id.magic)
        val vip: TextView = itemView.findViewById(R.id.vip)
        val times: TextView = itemView.findViewById(R.id.times)

    }

}

data class MovieMinimal(
    val id: Int,
    val title: String,
    val rating: Int
)

class SessionGroup(
    val id: String,
    val movieId: Int,
    val movieTitle: String,
    val movieRating: Int,
    val room: Int,
    val format: String,
    val version: String,
    val magic: Boolean,
    val vip: Boolean,
    val sessions: MutableList<Session> = mutableListOf()
) {

    fun add(session: Session) {
        if (!isValid(session)) {
            return
        }
        sessions += session
    }

    fun clear() {
        sessions.clear()
    }

    /**
     * Checks if the given session is valid for the current group.
     */
    private fun isValid(session: Session) =
        session.movieId != session.movieId || session.format == format ||
            session.version == version || session.magic == magic || session.vip == vip
}