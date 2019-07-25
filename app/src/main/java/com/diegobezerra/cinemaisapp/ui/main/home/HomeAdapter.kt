package com.diegobezerra.cinemaisapp.ui.main.home

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.bumptech.glide.Priority
import com.diegobezerra.cinemaisapp.GlideApp
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.ui.main.home.HomeViewHolder.BannersViewHolder
import com.diegobezerra.cinemaisapp.ui.main.home.HomeViewHolder.HeaderViewHolder
import com.diegobezerra.cinemaisapp.ui.main.home.HomeViewHolder.PlayingMoviesViewHolder
import com.diegobezerra.cinemaisapp.ui.main.home.HomeViewHolder.UpcomingAllViewHolder
import com.diegobezerra.cinemaisapp.ui.main.home.HomeViewHolder.UpcomingMovieViewHolder
import com.diegobezerra.cinemaisapp.util.ImageUtils
import com.diegobezerra.cinemaisapp.util.NetworkUtils
import com.diegobezerra.cinemaisapp.widget.AutoSlideViewPager
import com.diegobezerra.core.cinemais.domain.model.Banner
import com.diegobezerra.core.cinemais.domain.model.Banner.Action.MOVIE
import com.diegobezerra.core.cinemais.domain.model.Cinemas
import com.diegobezerra.core.cinemais.domain.model.Home
import com.diegobezerra.core.cinemais.domain.model.Movie

class HomeAdapter(
    private val homeViewModel: HomeViewModel
) : ListAdapter<Any, HomeViewHolder>(HomeDiff) {

    companion object {
        private const val VIEW_TYPE_PLAYING_HEADER = 0
        private const val VIEW_TYPE_UPCOMING_HEADER = 1
        private const val VIEW_TYPE_BANNERS = 2
        private const val VIEW_TYPE_PLAYING_MOVIES = 3
        private const val VIEW_TYPE_UPCOMING_MOVIE = 4
        private const val VIEW_TYPE_UPCOMING_ALL = 5

        // This is used to save and restore playing movies recycler view horizontal
        // scroll position
        private const val PLAYING_MOVIES_SCROLL = "playing_movies_scroll"
        private const val CURRENT_BANNER_INDEX = "current_banner_index"
    }

    private val playingMoviesAdapter by lazy {
        PlayingMoviesAdapter(homeViewModel)
    }
    private var isWifiConnection: Boolean = false
    private val viewPool = RecyclerView.RecycledViewPool()

    var currentBannerIndex = 0
    private val bannerPageChangeListener = object : SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            currentBannerIndex = position
        }
    }

    var playingMoviesScroll = 0
    var data: Home = Home(
        backdrop = "",
        banners = emptyList(),
        playingMovies = emptyList(),
        upcomingMovies = emptyList(),
        cinemas = Cinemas()
    )
        set(value) {
            field = value
            submitList(buildList())
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeViewHolder {
        val context = parent.context
        isWifiConnection = NetworkUtils.isWifiConnection(parent.context)
        val inflater = LayoutInflater.from(context)
        return when (viewType) {
            VIEW_TYPE_BANNERS -> BannersViewHolder(
                inflater.inflate(R.layout.item_home_banners, parent, false)
            )
            VIEW_TYPE_PLAYING_HEADER -> HeaderViewHolder(
                inflater.inflate(R.layout.item_home_playing_header, parent, false)
            )
            VIEW_TYPE_PLAYING_MOVIES -> PlayingMoviesViewHolder(
                inflater.inflate(R.layout.item_home_playing_movies, parent, false)
            )
            VIEW_TYPE_UPCOMING_HEADER -> HeaderViewHolder(
                inflater.inflate(R.layout.item_home_upcoming_header, parent, false)
            )
            VIEW_TYPE_UPCOMING_MOVIE -> UpcomingMovieViewHolder(
                inflater.inflate(R.layout.item_home_upcoming_movie, parent, false)
            )
            VIEW_TYPE_UPCOMING_ALL -> UpcomingAllViewHolder(
                inflater.inflate(R.layout.item_home_upcoming_all, parent, false)
            )
            else -> throw IllegalStateException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(
        holder: HomeViewHolder,
        position: Int
    ) {
        when (holder) {
            is BannersViewHolder -> holder.apply {
                val adapter =
                    BannersAdapter(getItem(position) as List<Banner>)
                pager.adapter = adapter
                pager.currentItem = currentBannerIndex
                pager.removeOnPageChangeListener(bannerPageChangeListener)
                pager.addOnPageChangeListener(bannerPageChangeListener)
                indicator.viewPager = pager
                adapter.setListener {
                    // NOTE: Movie is currently the only action the app supports.
                    // Anything else will ask user to choose a browser-like app.
                    when (it.action) {
                        MOVIE -> homeViewModel.onMovieClicked(it.resourceId)
                        else -> {
                            if (it.htmlUrl.isNotEmpty()) {
                                itemView.context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(it.htmlUrl)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            is PlayingMoviesViewHolder -> holder.apply {
                recyclerView.run {
                    adapter = playingMoviesAdapter
                    setRecycledViewPool(viewPool)
                    setOnScrollListener(
                        object : OnScrollListener() {
                            override fun onScrollStateChanged(
                                recyclerView: RecyclerView,
                                newState: Int
                            ) {
                                if (newState == SCROLL_STATE_IDLE) {
                                    playingMoviesScroll = computeHorizontalScrollOffset()
                                }
                            }
                        }
                    )

                    // Make sure our scroll is restored
                    if (playingMoviesScroll != 0) {
                        post { scrollBy(playingMoviesScroll, 0) }
                    }
                }
            }

            is UpcomingMovieViewHolder -> holder.apply {
                val movie = getItem(position) as Movie
                title.text = movie.title
                synopsis.text = movie.synopsis

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

                itemView.setOnClickListener {
                    homeViewModel.onMovieClicked(movie.id)
                }
            }

            is UpcomingAllViewHolder -> holder.apply {
                itemView.setOnClickListener {
                    homeViewModel.onShowAllUpcomingClicked()
                }
            }

            is HeaderViewHolder -> Unit
        }
    }

    override fun onViewRecycled(holder: HomeViewHolder) {
        if (holder is BannersViewHolder) {
            currentBannerIndex = holder.pager.currentItem
        } else if (holder is PlayingMoviesViewHolder) {
            playingMoviesScroll = holder.recyclerView.computeHorizontalScrollOffset()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is PlayingMoviesHeader -> VIEW_TYPE_PLAYING_HEADER
            is UpcomingMoviesHeader -> VIEW_TYPE_UPCOMING_HEADER
            is BannersViewHolder -> VIEW_TYPE_BANNERS
            is List<*> -> {
                return if (item.isNotEmpty() && item[0] is Movie) {
                    VIEW_TYPE_PLAYING_MOVIES
                } else if (item.isNotEmpty() && item[0] is Banner) {
                    VIEW_TYPE_BANNERS
                } else {
                    throw IllegalStateException("Unknown view type at position $position")
                }
            }
            is Movie -> VIEW_TYPE_UPCOMING_MOVIE
            is UpcomingMoviesAll -> VIEW_TYPE_UPCOMING_ALL
            else -> throw IllegalStateException("Unknown view type at position $position")
        }
    }

    fun save(outState: Bundle) {
        outState.putInt(CURRENT_BANNER_INDEX, currentBannerIndex)
        outState.putInt(PLAYING_MOVIES_SCROLL, playingMoviesScroll)
    }

    fun restore(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            currentBannerIndex = it.getInt(CURRENT_BANNER_INDEX)
            playingMoviesScroll = it.getInt(PLAYING_MOVIES_SCROLL)
        }
    }

    private fun buildList(): List<Any> {
        val result = mutableListOf<Any>()
        if (data.banners.isNotEmpty()) {
            result.add(data.banners)
        }
        if (data.playingMovies.isNotEmpty()) {
            playingMoviesAdapter.submitList(data.playingMovies)
            result.add(PlayingMoviesHeader)
            result.add(data.playingMovies)
        }
        if (data.upcomingMovies.isNotEmpty()) {
            result.add(UpcomingMoviesHeader)
            result.addAll(data.upcomingMovies)
            result.add(UpcomingMoviesAll)
        }
        return result
    }
}

object PlayingMoviesHeader

object UpcomingMoviesHeader

object UpcomingMoviesAll

sealed class HomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    class HeaderViewHolder(itemView: View) : HomeViewHolder(itemView)

    class BannersViewHolder(itemView: View) : HomeViewHolder(itemView) {
        val pager: AutoSlideViewPager = itemView.findViewById(R.id.viewpager)
        val indicator: BannersIndicatorView = itemView.findViewById(R.id.indicator)
    }

    class PlayingMoviesViewHolder(itemView: View) : HomeViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)
    }

    class UpcomingMovieViewHolder(itemView: View) : HomeViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val poster: ImageView = itemView.findViewById(R.id.poster)
        val synopsis: TextView = itemView.findViewById(R.id.synopsis)
    }

    class UpcomingAllViewHolder(itemView: View) : HomeViewHolder(itemView)
}

object HomeDiff : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(
        oldItem: Any,
        newItem: Any
    ): Boolean {
        return when {
            oldItem === PlayingMoviesHeader && newItem === PlayingMoviesHeader -> true
            oldItem === UpcomingMoviesHeader && newItem === UpcomingMoviesHeader -> true
            oldItem === UpcomingMoviesAll && newItem === UpcomingMoviesAll -> true
            oldItem is Movie && newItem is Movie -> oldItem.id == newItem.id
            oldItem is List<*> && newItem is List<*> -> oldItem == newItem
            else -> false
        }
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            oldItem is List<*> && newItem is List<*> -> oldItem == newItem
            oldItem is Movie && newItem is Movie -> oldItem == newItem
            oldItem is Banner && newItem is Banner -> oldItem == newItem
            else -> true
        }
    }
}