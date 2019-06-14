package com.diegobezerra.cinemaisapp.ui.main.home

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.diegobezerra.cinemaisapp.GlideApp
import com.diegobezerra.cinemaisapp.GlideOptions
import com.diegobezerra.cinemaisapp.GlideOptions.bitmapTransform
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.util.ImageUtils
import com.diegobezerra.cinemaisapp.util.NetworkUtils
import com.diegobezerra.core.cinemais.domain.model.Movie

class PlayingMoviesAdapter(
    private val homeViewModel: HomeViewModel
) : RecyclerView.Adapter<PlayingMovieViewHolder>() {

    var list: List<Movie> = emptyList()


    private val crossFade = BitmapTransitionOptions.withCrossFade()
    private var placeholder: Drawable? = null
    private var posterOptions: GlideOptions? = null
    private var isWifiConnection: Boolean = false

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlayingMovieViewHolder {
        if (placeholder == null) {
            placeholder = ContextCompat.getDrawable(parent.context, R.drawable.poster_placeholder)
        }
        if (posterOptions == null) {
            posterOptions =
                bitmapTransform(ImageUtils.posterTransformation(parent.context.applicationContext))
        }
        isWifiConnection = NetworkUtils.isWifiConnection(parent.context)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_playing_movie, parent, false)
        return PlayingMovieViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PlayingMovieViewHolder,
        position: Int
    ) {

        holder.apply {
            val movie = list[position]
            movie.posters.best(isWifiConnection)?.let {
                GlideApp.with(itemView)
                    .asBitmap()
                    .load(it)
                    .placeholder(placeholder)
                    .apply(posterOptions!!)
                    .transition(crossFade)
                    .into(poster)
            }

            title.text = movie.title

            itemView.setOnClickListener {
                homeViewModel.onMovieClicked(movie.id)
            }
        }
    }

    override fun getItemCount(): Int = list.size
}

class PlayingMovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val poster: ImageView = itemView.findViewById(R.id.poster)
    val title: TextView = itemView.findViewById(R.id.title)
}