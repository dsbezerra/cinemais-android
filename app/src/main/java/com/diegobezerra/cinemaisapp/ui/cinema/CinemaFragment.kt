package com.diegobezerra.cinemaisapp.ui.cinema

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.transition.Fade.IN
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
import com.diegobezerra.cinemaisapp.ui.schedule.ScheduleAdapter
import com.diegobezerra.cinemaisapp.ui.schedule.ScheduleViewModel
import com.diegobezerra.cinemaisapp.ui.tickets.TicketsActivity
import com.diegobezerra.cinemaisapp.util.setupToolbarAsActionBar
import com.diegobezerra.cinemaisapp.widget.CinemaisTabLayout
import com.google.android.material.appbar.AppBarLayout
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class CinemaFragment : DaggerFragment() {

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
        val sessionsLayout = root.findViewById<ViewGroup>(R.id.sessions_layout)
        val name = root.findViewById<TextView>(R.id.cinema_name)
        val address = root.findViewById<TextView>(R.id.cinema_address)
        val pager = root.findViewById<ViewPager>(R.id.viewpager)
        val tabs = root.findViewById<CinemaisTabLayout>(R.id.tabs)


        setupToolbarAsActionBar(root, R.id.toolbar) {
            title = getString(R.string.title_theaters)
            setDisplayHomeAsUpEnabled(true)
        }

        val args = requireNotNull(arguments)
        val cinemaId = args.getInt(CINEMA_ID)

        viewModel.apply {
            loading.observe(this@CinemaFragment, Observer {
                progressBar.isGone = !it

                if (it) {
                    TransitionManager.beginDelayedTransition(root as ViewGroup)
                    appbar.isGone = true
                    tabs.isGone = true
                    pager.isGone = true
                }
            })

            schedule.observe(this@CinemaFragment, Observer {
                name.text = it.cinema.name

                pager.adapter = ScheduleAdapter(it, childFragmentManager)
                tabs.setupWithViewPager(pager)

                it.cinema.location?.let { location ->
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
                        .addTransition(Fade(IN))
                        .setStartDelay(400L)
                        .setDuration(400L)
                        .setInterpolator(FastOutSlowInInterpolator())
                )
                appbar.isGone = false
                tabs.isGone = false
                pager.isGone = false
            })

            setCinemaId(cinemaId)
        }

        val appBarMaxElevation = resources.getDimension(R.dimen.sessions_max_elevation)
        appbar.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { v, verticalOffset ->
                val ratio = Math.abs(verticalOffset).toFloat() / v.totalScrollRange.toFloat()
                val alpha = 1.0f - ratio
                cinemaLayout.alpha = alpha
                sessionsLayout.translationZ = ratio * appBarMaxElevation
            }
        )

        root.findViewById<View>(R.id.change)
            .apply {
                setOnClickListener {
                    back()
                }
            }

        root.findViewById<View>(R.id.tickets)
            .apply {
                setOnClickListener {
                    requireActivity().run {
                        TicketsActivity.startActivity(this, cinemaId)
                        overridePendingTransition(R.anim.slide_in, R.anim.fade_out)
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
}
