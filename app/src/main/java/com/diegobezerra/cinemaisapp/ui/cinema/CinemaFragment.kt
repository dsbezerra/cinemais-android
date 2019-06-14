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
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isGone
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
import com.diegobezerra.cinemaisapp.ui.main.MainFragment
import com.diegobezerra.cinemaisapp.ui.movie.playingcinemas.PlayingCinemasFragment.Companion.TITLES
import com.diegobezerra.cinemaisapp.ui.schedule.ScheduleAdapter
import com.diegobezerra.cinemaisapp.ui.tickets.TicketsActivity
import com.diegobezerra.cinemaisapp.ui.tickets.TicketsActivity.Companion.EXTRA_CINEMA_ID
import com.diegobezerra.cinemaisapp.ui.tickets.TicketsActivity.Companion.EXTRA_REVEAL_START_RADIUS
import com.diegobezerra.cinemaisapp.ui.tickets.TicketsActivity.Companion.EXTRA_REVEAL_X
import com.diegobezerra.cinemaisapp.ui.tickets.TicketsActivity.Companion.EXTRA_REVEAL_Y
import com.diegobezerra.cinemaisapp.util.setupToolbarAsActionBar
import com.diegobezerra.cinemaisapp.widget.CinemaActionView
import com.diegobezerra.cinemaisapp.widget.CinemaisTabLayout
import com.google.android.material.appbar.AppBarLayout
import javax.inject.Inject

class CinemaFragment : MainFragment() {

    companion object {

        const val CINEMA_ID = "arg.CINEMA_ID"

        fun newInstance(cinemaId: Int): CinemaFragment {
            return CinemaFragment().apply {
                arguments = Bundle().apply {
                    putInt(CINEMA_ID, cinemaId)
                }
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)
            .get(CinemaViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_cinema, container, false)
        val appbar = root.findViewById<AppBarLayout>(R.id.appbar)
        val progressBar = root.findViewById<View>(R.id.progress_bar)
        val cinemaLayout = root.findViewById<View>(R.id.cinema_layout)
        val cinemaActions = root.findViewById<ViewGroup>(R.id.cinema_actions)
        val sessionsLayout = root.findViewById<ViewGroup>(R.id.sessions_layout)
        val sessionCinemaName = root.findViewById<TextView>(R.id.session_cinema_name)
        val name = root.findViewById<TextView>(R.id.cinema_name)
        val address = root.findViewById<TextView>(R.id.cinema_address)
        val pager = root.findViewById<ViewPager>(R.id.viewpager)
        val tabs = root.findViewById<CinemaisTabLayout>(R.id.tabs)

        setupToolbarAsActionBar(root, R.id.toolbar) {
            title = getString(R.string.title_cinema)
            setDisplayHomeAsUpEnabled(true)
        }

        val args = requireNotNull(arguments)
        val cinemaId = args.getInt(CINEMA_ID)

        viewModel.apply {
            loading.observe(this@CinemaFragment, Observer {
                progressBar.isGone = !it

                if (it) {
                    TransitionManager.beginDelayedTransition(root as ViewGroup)
                    tabs.isGone = true
                    pager.isGone = true
                    sessionsLayout.isGone = true
                }
            })

            schedule.observe(this@CinemaFragment, Observer { schedule ->
                name.text = schedule.cinema.name
                sessionCinemaName.text = schedule.cinema.name

                val context = requireContext()
                val titleList = TITLES.map { context.getString(it) }
                pager.adapter = ScheduleAdapter(titleList, schedule, childFragmentManager)
                tabs.setupWithViewPager(pager)

                schedule.cinema.location?.let { location ->
                    address.text = location.addressLine
                    root.findViewById<View>(R.id.location).apply {
                        setOnClickListener {
                            val gmmIntentUri =
                                Uri.parse("geo:${location.latitude},${location.longitude}?z=17")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            val packageManager = requireActivity().packageManager
                            if (mapIntent.resolveActivity(packageManager) != null) {
                                startActivity(mapIntent)
                            }
                        }
                    }
                }

                TransitionManager.beginDelayedTransition(
                    root as ViewGroup, TransitionSet()
                        .addTransition(
                            Slide(Gravity.BOTTOM)
                                .addTarget(pager)
                        )
                        .addTransition(
                            Fade(Fade.IN)
                                .setStartDelay(200L)
                        )
                        .addTransition(
                            Slide(Gravity.TOP)
                                .setDuration(500L)
                                .addTarget(cinemaActions)
                        )
                        .setDuration(400L)
                        .setInterpolator(FastOutSlowInInterpolator())
                )
                tabs.isGone = false
                pager.isGone = false
                cinemaActions.isGone = false
                sessionsLayout.isGone = false
            })

            setDateString()
            setCinemaId(cinemaId)
        }

        val appBarMaxElevation = resources.getDimension(R.dimen.sessions_max_elevation)
        val sessionCinemaNameSpacing = resources.getDimension(R.dimen.spacing_medium)
        appbar.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { v, verticalOffset ->
                val ratio = Math.abs(verticalOffset).toFloat() / v.totalScrollRange.toFloat()
                val offsetValue = 1.0f - ratio

                cinemaLayout.alpha = offsetToProperty(offsetValue, 0.33f, 0.67f)
                sessionsLayout.translationZ = ratio * appBarMaxElevation

                val changedValue = offsetToProperty(offsetValue, 0.33f, 0f)
                sessionCinemaName.alpha = changedValue
                sessionCinemaName.translationX =
                    -sessionCinemaNameSpacing + (sessionCinemaNameSpacing * 2 * changedValue)
            }
        )

        root.findViewById<CinemaActionView>(R.id.tickets)
            .apply {
                setOnClickListener {
                    requireActivity().run {
                        val revealOpts = getRevealOptions()
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
            }

        root.findViewById<View>(R.id.info)
            .apply {
                setOnClickListener {
                    Toast.makeText(requireActivity(), "Indisponível", Toast.LENGTH_SHORT).show()
                }
            }

        return root
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
}
