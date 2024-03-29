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
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import androidx.viewpager.widget.ViewPager
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.base.RevealActivity
import com.diegobezerra.cinemaisapp.databinding.FragmentCinemaBinding
import com.diegobezerra.cinemaisapp.ui.main.MainFragment
import com.diegobezerra.cinemaisapp.ui.schedule.ScheduleAdapter
import com.diegobezerra.cinemaisapp.ui.schedule.filters.ScheduleFiltersAdapter
import com.diegobezerra.cinemaisapp.ui.tickets.TicketsActivity
import com.diegobezerra.cinemaisapp.ui.tickets.TicketsActivity.Companion.EXTRA_CINEMA_ID
import com.diegobezerra.cinemaisapp.util.setupToolbarAsActionBar
import com.diegobezerra.cinemaisapp.widget.CinemaActionView
import com.diegobezerra.cinemaisapp.widget.CinemaisTabLayout
import com.diegobezerra.cinemaisapp.widget.SpaceItemDecoration
import com.diegobezerra.core.cinemais.data.CinemaisService
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.shared.result.EventObserver
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.include_progress_bar.progress_bar
import timber.log.Timber
import kotlin.math.abs

@AndroidEntryPoint
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

    private val cinemaViewModel: CinemaViewModel by viewModels()

    private lateinit var viewPager: ViewPager
    private lateinit var tabs: CinemaisTabLayout

    private lateinit var ticketsAction: CinemaActionView

    private val filtersAdapter by lazy { ScheduleFiltersAdapter(cinemaViewModel) }

    private var cinemaLayout: View? = null
    private var sessionsLayout: ViewGroup? = null
    private var sessionsHeaderTranslationValues = intArrayOf(0, 0)

    private var currentSchedule: Schedule? = null

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

        root.findViewById<RecyclerView>(R.id.filters_recycler_view).apply {
            adapter = filtersAdapter
            addItemDecoration(
                SpaceItemDecoration(
                    resources.getDimension(R.dimen.spacing_small).toInt(),
                    resources.getDimension(R.dimen.spacing_medium).toInt(),
                    addSpaceAboveFirstItem = true,
                    addSpaceBelowLastItem = true
                )
            )
            setHasFixedSize(true)
        }

        setupToolbarAsActionBar(root, R.id.toolbar) {
            title = getString(R.string.title_cinema)
            setDisplayHomeAsUpEnabled(true)
        }.setNavigationOnClickListener { requireActivity().onBackPressed() }

        cinemaViewModel.filters.observe(viewLifecycleOwner) {
            filtersAdapter.submitList(it)
        }

        cinemaViewModel.navigateToSchedulePageAction.observe(
            viewLifecycleOwner,
            EventObserver { cinemaId ->
                openSchedulePage(cinemaId)
            })

        cinemaViewModel.navigateToTicketsAction.observe(
            viewLifecycleOwner,
            EventObserver { cinemaId ->
                openTickets(cinemaId)
            })

        cinemaViewModel.navigateToLocationAction.observe(
            viewLifecycleOwner,
            EventObserver { location ->
                openMaps(location.latitude, location.longitude)
            })

        cinemaViewModel.navigateToInfoAction.observe(viewLifecycleOwner,
            EventObserver {
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

        cinemaViewModel.schedule.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer

            initSessionsLayout()

            val noAdapter = viewPager.adapter == null
            val copy = it.copy()
            if (copy != currentSchedule) {
                val activity = requireActivity()
                viewPager.adapter =
                    ScheduleAdapter(childFragmentManager, activity, copy)
                currentSchedule = copy
            }
            if (noAdapter) {
                runDisplayTransition(view as ViewGroup)
            }
        })

        cinemaViewModel.loading.observe(viewLifecycleOwner) {
            progress_bar.isGone = cinemaViewModel.isCinemaLayoutVisible.get() || !it
        }
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
                .setStartDelay(400L)
                .setInterpolator(FastOutSlowInInterpolator())
        )
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
                -(v.paddingTop / 2f).toInt()
            )
        }
        updateSessionsLayout(0f)
    }

    private fun updateSessionsLayout(slideOffset: Float) {
        sessionsLayout?.let {
            val value =
                offsetToProperty(slideOffset, ALPHA_SESSIONS_CHANGEOVER, ALPHA_SESSIONS_MAX)
            it.translationX = sessionsHeaderTranslationValues[0] * value
            it.translationY = sessionsHeaderTranslationValues[1] * value

            val header = it.getChildAt(0)
            val name = it.getChildAt(1)
            val filter = it.getChildAt(2)
            name.alpha = value
            name.translationX =
                (header.right - header.left) * value
            filter.translationX = (filter.width * value * -2.2f)
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
            val intent = Intent(this, TicketsActivity::class.java).apply {
                putExtra(EXTRA_CINEMA_ID, cinemaId)
                putExtras(RevealActivity.makeReveal(ticketsAction.getRevealOptions(), null))
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
}
