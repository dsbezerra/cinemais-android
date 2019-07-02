package com.diegobezerra.cinemaisapp.ui.cinema

import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.fragment.app.FragmentTransaction
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import androidx.viewpager.widget.ViewPager
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.databinding.FragmentCinemaBinding
import com.diegobezerra.cinemaisapp.ui.main.MainFragment
import com.diegobezerra.cinemaisapp.ui.main.cinemas.CinemasFragment
import com.diegobezerra.cinemaisapp.ui.schedule.ScheduleAdapter
import com.diegobezerra.cinemaisapp.ui.tickets.TicketsActivity
import com.diegobezerra.cinemaisapp.ui.tickets.TicketsActivity.Companion.EXTRA_CINEMA_ID
import com.diegobezerra.cinemaisapp.ui.tickets.TicketsActivity.Companion.EXTRA_REVEAL_START_RADIUS
import com.diegobezerra.cinemaisapp.ui.tickets.TicketsActivity.Companion.EXTRA_REVEAL_X
import com.diegobezerra.cinemaisapp.ui.tickets.TicketsActivity.Companion.EXTRA_REVEAL_Y
import com.diegobezerra.cinemaisapp.util.setupToolbarAsActionBar
import com.diegobezerra.cinemaisapp.widget.CinemaActionView
import com.diegobezerra.cinemaisapp.widget.CinemaisTabLayout
import com.diegobezerra.core.cinemais.data.CinemaisService
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.event.EventObserver
import com.google.android.material.appbar.AppBarLayout
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.abs

class CinemaFragment : MainFragment() {

    companion object {

        const val TAG = "fragment.CINEMA"

        private const val ALPHA_HEADER_CHANGEOVER = 0.33f
        private const val ALPHA_HEADER_MAX = 0.67f

        private const val ALPHA_SESSIONS_CHANGEOVER = 0.75f
        private const val ALPHA_SESSIONS_MAX = 1f

        const val CINEMA_ID = "arg.CINEMA_ID"

        fun newArgs(cinemaId: Int): Bundle {
            return Bundle().apply {
                putInt(CINEMA_ID, cinemaId)
            }
        }

        fun newInstance(cinemaId: Int): CinemaFragment {
            return CinemaFragment().apply {
                arguments = newArgs(cinemaId)
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val cinemaViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)
            .get(CinemaViewModel::class.java)
    }

    private lateinit var viewPager: ViewPager
    private lateinit var tabs: CinemaisTabLayout

    private lateinit var ticketsAction: CinemaActionView

    private var cinemaLayout: View? = null
    private var sessionsLayout: ViewGroup? = null
    private var sessionsHeaderTranslationValues = intArrayOf(0, 0)

    private var schedule: Schedule? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentCinemaBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@CinemaFragment
            viewModel = cinemaViewModel
        }

        viewPager = binding.viewpager

        val root = binding.root
        tabs = root.findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        cinemaLayout = root.findViewById(R.id.cinema_layout)
        sessionsLayout = root.findViewById(R.id.sessions_layout)

        val progress = root.findViewById<View>(R.id.progress_bar)

        setupToolbarAsActionBar(root, R.id.toolbar) {
            title = getString(R.string.title_cinema)
            setDisplayHomeAsUpEnabled(true)
        }

        cinemaViewModel.loading.observe(this@CinemaFragment, Observer {
            progress.isGone = !it
            if (it) {
                setViewsIsGone(true)
            }
        })

        cinemaViewModel.navigateToSchedulePageAction.observe(this, EventObserver { cinemaId ->
            openSchedulePage(cinemaId)
        })

        cinemaViewModel.navigateToTicketsAction.observe(this, EventObserver { cinemaId ->
            openTickets(cinemaId)
        })

        cinemaViewModel.navigateToLocationAction.observe(this, EventObserver { location ->
            openMaps(location.latitude, location.longitude)
        })

        cinemaViewModel.navigateToInfoAction.observe(this, EventObserver {
            Toast.makeText(requireActivity(), "Indisponível", Toast.LENGTH_SHORT).show()
        })

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ticketsAction = view.findViewById(R.id.tickets)

        val appbar = view.findViewById<AppBarLayout>(R.id.appbar)
        appbar.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { v, verticalOffset ->
                val slideOffset =
                    abs(verticalOffset).toFloat() / v.totalScrollRange.toFloat()
                applyTransformation(slideOffset)
            }
        )

        cinemaViewModel.schedule.observe(this@CinemaFragment, Observer {
            it ?: return@Observer

            initSessionsLayout()
            if (schedule != it) {
                viewPager.adapter = ScheduleAdapter(childFragmentManager, requireActivity(), it)
                schedule = it
            }

            runDisplayTransition(view as ViewGroup)
        })
    }

    override fun onStart() {
        super.onStart()
        Timber.d("Loading details for cinema $arguments")
        cinemaViewModel.setCinemaId(requireNotNull(arguments).getInt(CINEMA_ID))
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            cinemaViewModel.setCinemaId(requireNotNull(arguments).getInt(CINEMA_ID))
        }
    }

    override fun onStop() {
        super.onStop()
        cinemaViewModel.setCinemaId(null)
    }

    private fun runDisplayTransition(root: ViewGroup) {
        TransitionManager.beginDelayedTransition(
            root, TransitionSet()
                .addTransition(
                    Slide(Gravity.BOTTOM)
                        .addTarget(viewPager)
                )
                .addTransition(
                    Slide(Gravity.TOP)
                        .setDuration(500L)
                        .addTarget(cinemaLayout!!)
                )
                .addTransition(
                    Fade(Fade.IN)
                        .setStartDelay(200L)
                )
                .setDuration(400L)
                .setInterpolator(FastOutSlowInInterpolator())
        )
        setViewsIsGone(false)
    }

    private fun setViewsIsGone(isGone: Boolean) {
        tabs.isGone = isGone
        viewPager.isGone = isGone
        cinemaLayout?.isGone = isGone
        sessionsLayout?.isGone = isGone
    }

    private fun applyTransformation(slideOffset: Float) {
        cinemaLayout?.alpha =
            offsetToProperty(1f - slideOffset, ALPHA_HEADER_CHANGEOVER, ALPHA_HEADER_MAX)
        updateSessionsLayout(slideOffset)
    }

    private fun initSessionsLayout() {
        sessionsLayout?.let { v ->
            sessionsHeaderTranslationValues = intArrayOf(
                resources.getDimension(R.dimen.sessions_header_margin_start).toInt(),
                -(v.paddingTop - v.paddingBottom / 2f).toInt()
            )
        }
        updateSessionsLayout(0f)
    }

    private fun updateSessionsLayout(slideOffset: Float) {
        sessionsLayout?.let {
            val headerValue =
                offsetToProperty(slideOffset, ALPHA_SESSIONS_CHANGEOVER, ALPHA_SESSIONS_MAX)
            it.translationX = sessionsHeaderTranslationValues[0] * headerValue
            it.translationY = sessionsHeaderTranslationValues[1] * headerValue

            val header = it.getChildAt(0)
            val name = it.getChildAt(1)
            val value =
                offsetToProperty(slideOffset, ALPHA_SESSIONS_CHANGEOVER, ALPHA_SESSIONS_MAX)
            name.alpha = value
            name.translationX =
                (header.right - header.left) * value
        }
    }

    private fun openSchedulePage(cinemaId: Int) {
        val url = "${CinemaisService.ENDPOINT}programacao/cinema.php?cc=$cinemaId"
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            // No-op
        }
    }

    private fun openTickets(cinemaId: Int) {
        requireActivity().run {
            val revealOpts = ticketsAction.getRevealOptions()
            val intent = Intent(this, TicketsActivity::class.java).apply {
                putExtra(EXTRA_CINEMA_ID, cinemaId)
                putExtra(EXTRA_REVEAL_X, revealOpts[0])
                putExtra(EXTRA_REVEAL_Y, revealOpts[1])
                putExtra(EXTRA_REVEAL_START_RADIUS, revealOpts[2])
            }
            val opts = ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
            startActivity(intent, opts)
        }
    }

    private fun openMaps(latitude: Double, longitude: Double) {
        val gmmIntentUri =
            Uri.parse("geo:$latitude,$longitude?z=17")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        val packageManager = requireActivity().packageManager
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        }
    }

    /**
     * Map a slideOffset (in the range `[-1, 1]`) to an property value based on the desired range.
     * For example, `offsetToProperty(0.5, 0.25, 1) = 0.33` because 0.5 is 1/3 of the way between 0.25
     * and 1. The result value is additionally clamped to the range `[0, 1]`.
     *
     * Borrowed from Google's I/O 2018 source
     */
    private fun offsetToProperty(value: Float, rangeMin: Float, rangeMax: Float): Float {
        return ((value - rangeMin) / (rangeMax - rangeMin)).coerceIn(0f, 1f)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                back()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun back() {
        requireActivity().run {
            onBackPressed()
        }
    }

    override fun title(): String? = ""

    override fun transition(ft: FragmentTransaction, to: String) {
        if (to == CinemasFragment.TAG) {
            ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
        } else {
            super.transition(ft, to)
        }
    }
}
