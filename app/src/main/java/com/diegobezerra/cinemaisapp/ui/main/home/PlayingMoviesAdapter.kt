package com.diegobezerra.cinemaisapp.ui.main.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.ui.main.home.PlayingMoviesAdapter.PlayingMovieViewHolder
import com.diegobezerra.cinemaisapp.ui.main.movies.MovieDiff
import com.diegobezerra.cinemaisapp.util.ImageUtils
import com.diegobezerra.cinemaisapp.util.NetworkUtils
import com.diegobezerra.core.cinemais.domain.model.Movie

class PlayingMoviesAdapter(
    private val homeViewModel: HomeViewModel
) : ListAdapter<Movie, PlayingMovieViewHolder>(MovieDiff) {

    private var isWifiConnection: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayingMovieViewHolder {
        isWifiConnection = NetworkUtils.isWifiConnection(parent.context)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_playing_movie, parent, false)
        return PlayingMovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayingMovieViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlayingMovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val poster: ImageView = itemView.findViewById(R.id.poster)
        val title: TextView = itemView.findViewById(R.id.title)

        fun bind(movie: Movie) {
            movie.posters.best(isWifiConnection)?.let { posterUrl ->
                ImageUtils.loadPoster(posterUrl, poster)
            }
            title.text = movie.title
            itemView.setOnClickListener {
                homeViewModel.onMovieClicked(movie.id)
            }
        }
    }
}

