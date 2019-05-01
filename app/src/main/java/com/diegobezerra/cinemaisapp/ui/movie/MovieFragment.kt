package com.diegobezerra.cinemaisapp.ui.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isGone
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import com.diegobezerra.cinemaisapp.GlideApp
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.ui.movie.playingcinemas.PlayingCinemasFragment
import com.diegobezerra.cinemaisapp.util.ImageUtils
import com.diegobezerra.cinemaisapp.util.setupToolbarAsActionBar
import com.diegobezerra.core.cinemais.domain.model.Movie
import com.diegobezerra.core.cinemais.domain.model.Posters
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import dagger.android.support.DaggerFragment
import fr.castorflex.android.circularprogressbar.CircularProgressBar
import kotlinx.android.synthetic.main.fragment_movie.cast
import kotlinx.android.synthetic.main.fragment_movie.direction
import kotlinx.android.synthetic.main.fragment_movie.executiveProduction
import kotlinx.android.synthetic.main.fragment_movie.genres
import kotlinx.android.synthetic.main.fragment_movie.originalTitle
import kotlinx.android.synthetic.main.fragment_movie.poster
import kotlinx.android.synthetic.main.fragment_movie.production
import kotlinx.android.synthetic.main.fragment_movie.ratingImage
import kotlinx.android.synthetic.main.fragment_movie.releaseRuntime
import kotlinx.android.synthetic.main.fragment_movie.screenplay
import kotlinx.android.synthetic.main.fragment_movie.synopsis
import kotlinx.android.synthetic.main.fragment_movie.title
import java.text.SimpleDateFormat
import javax.inject.Inject

private val FORMAT = SimpleDateFormat("dd MMMM")

class MovieFragment : DaggerFragment() {

    companion object {

        const val MOVIE_ID = "arg.MOVIE_ID"

        fun newInstance(id: Int): MovieFragment {
            return MovieFragment().apply {
                arguments = Bundle().apply {
                    putInt(MOVIE_ID, id)
                }
            }
        }

    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)
            .get(MovieViewModel::class.java)
    }

    private lateinit var toolbar: Toolbar

    private lateinit var playingCinemasFragment: PlayingCinemasFragment
    private lateinit var playingCinemasSheet: ViewGroup
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_movie, container, false)
        val progressBar = root.findViewById<CircularProgressBar>(R.id.progress_bar)
        val swipeRefreshLayout = root.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)
        playingCinemasSheet = root.findViewById(R.id.playing_cinemas_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(playingCinemasSheet)

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }

        val args = requireNotNull(arguments)

        toolbar = setupToolbarAsActionBar(root, R.id.toolbar) {
            title = getString(R.string.title_movie)
            setDisplayHomeAsUpEnabled(true)
        }

        viewModel.apply {
            loading.observe(this@MovieFragment, Observer {
                val firstLoad = viewModel.movie.value == null
                if (!firstLoad) {
                    swipeRefreshLayout.isRefreshing = it
                } else {
                    progressBar.isGone = !it
                }
            })

            movie.observe(this@MovieFragment, Observer {
                initMovie(it)
            })

            setMovieId(args.getInt(MOVIE_ID))
        }

        childFragmentManager.findFragmentById(R.id.playing_cinemas_sheet)
            ?.let {
                playingCinemasFragment = it as PlayingCinemasFragment
            }

        return root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                requireActivity().onBackPressed()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initMovie(movie: Movie) {
        initPoster(movie.posters)
        initInfo(movie)
        initRating(movie.rating)
        initGenres(movie.genres)
        initSynopsis(movie.synopsis)
        initCast(movie.cast)
        initScreenplay(movie.screenplay)
        initProduction(movie.production)
        initExecutiveProduction(movie.executiveProduction)
        initDirection(movie.direction)

        if (movie.isPlaying()) {
            playingCinemasFragment.peek(movie.id)
        } else {
            playingCinemasSheet.isGone = true
        }
    }

    private fun initPoster(posters: Posters) {
        if (!posters.medium.isNullOrEmpty()) {
            GlideApp.with(requireContext())
                .asBitmap()
                .load(posters.medium)
                .placeholder(ImageUtils.placeholder(requireActivity()))
                .transition(withCrossFade())
                .into(poster)
        }
    }

    private fun initInfo(movie: Movie) {
//        toolbar.title = movie.title
        title.text = movie.title

        if (movie.originalTitle != "" && movie.originalTitle != movie.title) {
            originalTitle.text = getString(R.string.original_title, movie.originalTitle)
            originalTitle.isGone = false
        } else {
            originalTitle.isGone = true
        }

        if (movie.releaseDate != null && movie.runtime > 0) {
            releaseRuntime.text = "${FORMAT.format(movie.releaseDate)}  •  ${movie.runtime} min"
        } else if (movie.releaseDate != null) {
            releaseRuntime.text = "${FORMAT.format(movie.releaseDate)}"
        } else if (movie.rating > 0) {
            releaseRuntime.text = "${movie.runtime} min"
        }
    }

    private fun initRating(rating: Int) {
        if (rating != 0) {
            when (rating) {
                -1 -> {
                    ratingImage.setImageResource(R.drawable.ic_rating_l)
                }
                10 -> {
                    ratingImage.setImageResource(R.drawable.ic_rating_10)
                }
                12 -> {
                    ratingImage.setImageResource(R.drawable.ic_rating_12)
                }
                14 -> {
                    ratingImage.setImageResource(R.drawable.ic_rating_14)
                }
                16 -> {
                    ratingImage.setImageResource(R.drawable.ic_rating_16)
                }
                18 -> {
                    ratingImage.setImageResource(R.drawable.ic_rating_18)
                }
            }
        }
    }

    private fun initGenres(list: List<String>) {
        genres.setContent(list.joinToString(", "))
        genres.isGone = list.isEmpty()
    }

    private fun initSynopsis(text: String) {
        synopsis.isGone = text == ""
        synopsis.text = text
    }

    private fun initCast(list: List<String>) {
        cast.setContent(list.joinToString(", "))
        cast.isGone = list.isEmpty()
    }

    private fun initScreenplay(list: List<String>) {
        screenplay.setContent(list.joinToString(", "))
        screenplay.isGone = list.isEmpty()
    }

    private fun initProduction(list: List<String>) {
        production.setContent(list.joinToString(", "))
        production.isGone = list.isEmpty()
    }

    private fun initExecutiveProduction(list: List<String>) {
        executiveProduction.setContent(list.joinToString(", "))
        executiveProduction.isGone = list.isEmpty()
    }

    private fun initDirection(list: List<String>) {
        direction.setContent(list.joinToString(", "))
        direction.isGone = list.isEmpty()
    }

    fun onBackPressed(): Boolean {
        return if (bottomSheetBehavior.state == STATE_EXPANDED) {
            bottomSheetBehavior.state = STATE_COLLAPSED
            true
        } else {
            false
        }
    }
}
