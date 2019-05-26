package com.diegobezerra.cinemaisapp.ui.main.movies

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.signature.ObjectKey
import com.diegobezerra.cinemaisapp.GlideApp
import com.diegobezerra.cinemaisapp.GlideOptions
import com.diegobezerra.cinemaisapp.GlideOptions.bitmapTransform
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.ui.main.movies.MoviesAdapter.MovieViewHolder
import com.diegobezerra.cinemaisapp.ui.main.movies.MoviesFragment.Companion.UPCOMING
import com.diegobezerra.cinemaisapp.util.ImageUtils
import com.diegobezerra.core.cinemais.domain.model.Movie
import com.diegobezerra.core.util.DateUtils
import com.diegobezerra.core.util.DateUtils.Companion.BRAZIL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

class MoviesAdapter(
    private val moviesViewModel: TabMoviesViewModel,
    private val type: Int,
    var isWifiConnection: Boolean = false
) : RecyclerView.Adapter<MovieViewHolder>() {

    companion object {
        private val FORMAT = SimpleDateFormat("d MMM yyyy", BRAZIL)
        private val NO_YEAR_FORMAT = SimpleDateFormat("d MMM", BRAZIL)
    }

    var list: List<Movie> = emptyList()

    private val currentYear = Date(System.currentTimeMillis()).year
    private val crossFade = BitmapTransitionOptions.withCrossFade()
    private var placeholder: Drawable? = null
    private var posterOptions: GlideOptions? = null

    // This will ensure cache invalidation at least one time per week.
    // Necessary because images here can change and URLs will still be the same.
    //
    // Alternatively we could implement our own custom fetcher to detect modifications
    // and ask Glide to use it, but this will serve.
    private val signature: ObjectKey by lazy {
        val current = DateUtils.calendarAtStartOfDay().timeInMillis
        val expiration = TimeUnit.DAYS.toMillis(7).toDouble()
        ObjectKey(Math.round(current / expiration).toString())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        if (placeholder == null) {
            placeholder = ContextCompat.getDrawable(parent.context, R.drawable.poster_placeholder)
        }
        if (posterOptions == null) {
            posterOptions =
                bitmapTransform(ImageUtils.posterTransformation(parent.context.applicationContext))
        }
        return MovieViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_grid_movie,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {

        holder.apply {
            val movie = list[position]
            val context = itemView.context
            movie.posters.best(isWifiConnection)?.let {
                if (posterOptions == null) {
                    posterOptions =
                        bitmapTransform(ImageUtils.posterTransformation(context.applicationContext))
                }
                GlideApp.with(context)
                    .asBitmap()
                    .load(it)
                    .placeholder(placeholder)
                    .apply(posterOptions!!)
                    .transition(crossFade)
                    .signature(signature)
                    .into(poster)
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

    override fun getItemCount() = list.size

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
