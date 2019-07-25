package com.diegobezerra.cinemaisapp.ui.main.movies

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Priority
import com.diegobezerra.cinemaisapp.GlideApp
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.ui.main.movies.MoviesAdapter.MovieViewHolder
import com.diegobezerra.cinemaisapp.ui.main.movies.MoviesFragment.Companion.UPCOMING
import com.diegobezerra.cinemaisapp.util.ImageUtils
import com.diegobezerra.core.cinemais.domain.model.Movie
import com.diegobezerra.core.util.DateUtils.Companion.BRAZIL
import java.text.SimpleDateFormat
import java.util.Date

class MoviesAdapter(
    private val moviesViewModel: TabMoviesViewModel,
    private val type: Int,
    private var isWifiConnection: Boolean = false
) : ListAdapter<Movie, MovieViewHolder>(MovieDiff) {

    companion object {
        private val FORMAT = SimpleDateFormat("d 'de' MMMM 'de' yyyy", BRAZIL)
        private val NO_YEAR_FORMAT = SimpleDateFormat("d 'de' MMMM", BRAZIL)
    }

    private val currentYear = Date(System.currentTimeMillis()).year

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MovieViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_grid_movie,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {

        holder.apply {
            val movie = getItem(position)
            movie.posters.best(isWifiConnection)?.let {
                ImageUtils.loadPoster(it, poster)
                // NOTE(diego): isWifiConnection makes sure we load the best image here, but in
                // the next time the user starts the app without a network connection he will never
                // see the cached large poster.
                // To avoid this we will also download the medium poster (temporary).
                if (isWifiConnection) {
                    GlideApp.with(itemView.context)
                        .load(movie.posters.medium)
                        .priority(Priority.LOW)
                        .preload()
                }
            }

            title.text = movie.title
            release.isGone = movie.releaseDate == null
            movie.releaseDate?.let {
                release.text = if (it.year == currentYear)
                    NO_YEAR_FORMAT.format(it)
                else
                    FORMAT.format(it)
            }

            itemView.setOnClickListener {
                moviesViewModel.onMovieClicked(movie.id)
            }
        }
    }

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val poster: ImageView = itemView.findViewById(R.id.poster)
        val title: TextView = itemView.findViewById(R.id.title)
        val release: TextView = itemView.findViewById(R.id.release)

        private val textContainer: LinearLayout = itemView.findViewById(R.id.text_container)

        init {
            val lp = textContainer.layoutParams
            if (type == UPCOMING) {
                lp.height =
                    itemView.resources.getDimension(R.dimen.grid_movie_text_upcoming_height).toInt()
            }
        }
    }
}

object MovieDiff : DiffUtil.ItemCallback<Movie>() {
    override fun areItemsTheSame(
        oldItem: Movie,
        newItem: Movie
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem == newItem
    }
}