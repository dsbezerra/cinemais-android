package com.diegobezerra.cinemaisapp.ui.main.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.diegobezerra.cinemaisapp.GlideApp
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.util.ImageUtils
import com.diegobezerra.core.cinemais.domain.model.Movie

class PlayingMoviesAdapter(
    private val homeViewModel: HomeViewModel
) : RecyclerView.Adapter<PlayingMovieViewHolder>() {

    var list: List<Movie> = emptyList()

    private val crossFade = BitmapTransitionOptions.withCrossFade()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlayingMovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_playing_movie, parent, false)
        return PlayingMovieViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PlayingMovieViewHolder,
        position: Int
    ) {
        holder.apply {
            val item = list[position]
            if (!item.posters.medium.isNullOrEmpty()) {
                GlideApp.with(itemView)
                    .asBitmap()
                    .load(item.posters.medium)
                    .placeholder(ImageUtils.placeholder(itemView.context))
                    .transition(crossFade)
                    .into(poster)
            }

            itemView.setOnClickListener {
                homeViewModel.onMovieClicked(item.id)
            }
        }
    }

    override fun getItemCount(): Int = list.size
}

class PlayingMovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val poster: ImageView = itemView.findViewById(R.id.poster)
}