package com.diegobezerra.cinemaisapp.ui.movie

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isGone
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.diegobezerra.cinemaisapp.GlideApp
import com.diegobezerra.cinemaisapp.GlideOptions.bitmapTransform
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.ui.movie.playingcinemas.PlayingCinemasFragment
import com.diegobezerra.cinemaisapp.util.setupToolbarAsActionBar
import com.diegobezerra.core.cinemais.domain.model.Movie
import com.diegobezerra.core.cinemais.domain.model.Posters
import com.diegobezerra.core.util.DateUtils.Companion.BRAZIL
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import dagger.android.support.DaggerFragment
import fr.castorflex.android.circularprogressbar.CircularProgressBar
import kotlinx.android.synthetic.main.fragment_movie.backdrop
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
import kotlinx.android.synthetic.main.fragment_movie.scroll
import kotlinx.android.synthetic.main.fragment_movie.synopsis
import kotlinx.android.synthetic.main.fragment_movie.title
import kotlinx.android.synthetic.main.include_movie_appbar.appbar
import kotlinx.android.synthetic.main.include_movie_appbar.cinemais_border
import kotlinx.android.synthetic.main.include_trailer.trailer
import java.text.SimpleDateFormat
import java.util.Calendar
import javax.inject.Inject

private val FORMAT = SimpleDateFormat("dd MMMM", BRAZIL)

class MovieFragment : DaggerFragment() {

    companion object {

        const val MOVIE_ID = "arg.MOVIE_ID"

        // 1 second delay peek for playing rooms sheet
        const val PEEK_DELAY = 1000L

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
    private var displayingTitleInToolbar = false

    private val peekHandler = Handler()
    private var peekRunnable: Runnable? = null
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
            title = null
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

    override fun onDestroy() {
        super.onDestroy()

        peekHandler.removeCallbacks(peekRunnable)
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
        requireContext().run {
            val weekday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            val image = movie.images.getBackdrop(weekday) ?: movie.images.getPoster(weekday)
            ?: movie.posters.large
            GlideApp.with(this)
                .asBitmap()
                .load(image)
                .transition(withCrossFade())
                .into(backdrop)
        }
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

        movie.trailer?.let {
            if (!it.isYoutube()) {
                return
            }
            trailer.apply {
                isGone = false
                setOnClickListener { _ ->
                    val youtubeUrl = "https://youtube.com/watch?v=${it.id}"
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl)))
                    } catch (e: Exception) {
                        // No-op
                    }
                }
            }
        }

        cinemais_border.scaleX = 0f
        cinemais_border.visibility = View.VISIBLE

        if (movie.isPlaying()) {
            peekRunnable = Runnable {
                playingCinemasFragment.peek(movie.id)
            }
            peekHandler.postDelayed(peekRunnable, PEEK_DELAY)
        } else {
            playingCinemasSheet.isGone = true
        }
    }

    private fun initPoster(posters: Posters) {
        if (!posters.medium.isNullOrEmpty()) {
            requireContext().run {
                GlideApp.with(this)
                    .asBitmap()
                    .load(posters.medium)
                    .apply(bitmapTransform(RoundedCorners(resources.getDimension(R.dimen.spacing_small).toInt())))
                    .transition(withCrossFade())
                    .into(poster)
            }
        }
    }

    private fun initInfo(movie: Movie) {
        title.text = movie.title

        if (movie.originalTitle != "" && movie.originalTitle != movie.title) {
            originalTitle.text = getString(R.string.original_title, movie.originalTitle)
            originalTitle.isGone = false
        } else {
            originalTitle.isGone = true
        }

        if (movie.releaseDate != null && movie.runtime > 0) {
            releaseRuntime.text = getString(
                R.string.release_with_runtime,
                FORMAT.format(movie.releaseDate),
                movie.runtime
            )
        } else if (movie.releaseDate != null) {
            releaseRuntime.text = FORMAT.format(movie.releaseDate)
        } else if (movie.rating > 0) {
            releaseRuntime.text = getString(R.string.runtime_only, movie.runtime)
        }

        var toolbarAnimator: ValueAnimator? = null
        val toolbarColors = getToolbarColors()
        val argbEvaluator = ArgbEvaluator()
        scroll.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            val displayTitle = scrollY >= backdrop.bottom
            if (displayingTitleInToolbar && !displayTitle) {
                toolbarAnimator?.cancel()
                toolbarAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
                    addUpdateListener {
                        val value = animatedValue as Float
                        appbar.setBackgroundColor(argbEvaluator.evaluate(value, toolbarColors[0], toolbarColors[1]) as Int)
                        cinemais_border.scaleX = value
                    }
                    start()
                }
                displayingTitleInToolbar = false
                toolbar.title = null
            } else if (displayTitle && !displayingTitleInToolbar) {
                toolbarAnimator?.cancel()
                toolbarAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                    addUpdateListener {
                        val value = animatedValue as Float
                        appbar.setBackgroundColor(argbEvaluator.evaluate(value, toolbarColors[0], toolbarColors[1]) as Int)
                        cinemais_border.scaleX = value
                    }
                    start()
                }
                displayingTitleInToolbar = true
                toolbar.title = movie.title
            } else {
                // No-op.
            }
        }
    }

    private fun initRating(rating: Int) {
        if (rating != 0) {
            var resId = 0
            when (rating) {
                -1 -> resId = R.drawable.ic_rating_l
                10 -> resId = R.drawable.ic_rating_10
                12 -> resId = R.drawable.ic_rating_12
                14 -> resId = R.drawable.ic_rating_14
                16 -> resId = R.drawable.ic_rating_16
                18 -> resId = R.drawable.ic_rating_18
            }
            ratingImage.setImageResource(resId)
        }
    }

    private fun initGenres(list: List<String>) {
        genres.content = list.joinToString(", ")
        genres.isGone = list.isEmpty()
    }

    private fun initSynopsis(text: String) {
        synopsis.isGone = text == ""
        synopsis.text = text
    }

    private fun initCast(list: List<String>) {
        cast.content = list.joinToString(", ")
        cast.isGone = list.isEmpty()
    }

    private fun initScreenplay(list: List<String>) {
        screenplay.content = list.joinToString(", ")
        screenplay.isGone = list.isEmpty()
    }

    private fun initProduction(list: List<String>) {
        production.content = list.joinToString(", ")
        production.isGone = list.isEmpty()
    }

    private fun initExecutiveProduction(list: List<String>) {
        executiveProduction.content = list.joinToString(", ")
        executiveProduction.isGone = list.isEmpty()
    }

    private fun initDirection(list: List<String>) {
        direction.content = list.joinToString(", ")
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

    private fun getToolbarColors(): IntArray {
        val attrs = requireContext().obtainStyledAttributes(
            intArrayOf(
                R.attr.background_color_transparent,
                R.attr.background_color
            )
        )
        val startColor = attrs.getColor(0, 0)
        val endColor = attrs.getColor(1, 1)
        attrs.recycle()
        return intArrayOf(startColor, endColor)
    }
}
