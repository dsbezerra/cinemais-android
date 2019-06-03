package com.diegobezerra.cinemaisapp.ui.movie.playingcinemas

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.view.isGone
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import androidx.viewpager.widget.ViewPager
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.ui.movie.playingcinemas.PlayingCinemasViewModel.Companion.LOADING_NONE
import com.diegobezerra.cinemaisapp.ui.movie.playingcinemas.PlayingCinemasViewModel.Companion.STATE_SCHEDULE
import com.diegobezerra.cinemaisapp.ui.schedule.ScheduleAdapter
import com.diegobezerra.cinemaisapp.widget.CinemaisTabLayout
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class PlayingCinemasFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)
            .get(PlayingCinemasViewModel::class.java)
    }

    private val playingCinemasAdapter by lazy { PlayingCinemasAdapter(viewModel) }

    private lateinit var sheet: ViewGroup
    private lateinit var sheetHeader: TextView
    private lateinit var sheetDescription: TextView
    private lateinit var resetArrow: View
    private lateinit var expandCollapseArrow: View
    private lateinit var tabs: CinemaisTabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var recyclerView: RecyclerView

    private lateinit var progressBar: View

    private var behavior: BottomSheetBehavior<*>? = null

    // Added to prevent adapter recreations
    private var prevSchedule: Schedule? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_playing_cinemas, container, false)
        initViews(root)

        viewModel.apply {

            loading.observe(this@PlayingCinemasFragment, Observer {
                progressBar.isGone = it == LOADING_NONE
            })

            currentCinema.observe(this@PlayingCinemasFragment, Observer {
                val text = if (it != null) {
                    getString(R.string.label_cinema, it.name, it.federativeUnit)
                } else {
                    null
                }
                sheetDescription.text = text
            })

            cinemas.observe(this@PlayingCinemasFragment, Observer {
                playingCinemasAdapter.data = it
                playingCinemasAdapter.notifyDataSetChanged()
            })

            schedule.observe(this@PlayingCinemasFragment, Observer {
                if (it != prevSchedule) {
                    val context = requireContext()
                    val titles = listOf<String>(
                        context.getString(R.string.today),
                        context.getString(R.string.tomorrow)
                    )
                    viewPager.adapter = ScheduleAdapter(titles, it, childFragmentManager)
                    prevSchedule = it
                }
            })

            state.observe(this@PlayingCinemasFragment, Observer {
                // Make sure our views are visible just in case the last onSlide
                // set them to invisible.
                interpolateChildViews(1f)
                updateViews(it)
            })

        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        behavior = BottomSheetBehavior.from(sheet)
        behavior?.setBottomSheetCallback(
            object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {}

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    interpolateChildViews(slideOffset)
                }
            }
        )

        expandCollapseArrow.setOnClickListener {
            toggle()
        }
    }

    private fun initViews(root: View) {
        sheet = root.findViewById(R.id.playing_cinemas_sheet)
        resetArrow = root.findViewById(R.id.reset_arrow)
        expandCollapseArrow = root.findViewById(R.id.expand_or_collapse_arrow)
        sheetHeader = root.findViewById(R.id.sheet_header)
        sheetDescription = root.findViewById(R.id.sheet_description)
        recyclerView = root.findViewById(R.id.recyclerView)
        recyclerView.run {
            adapter = playingCinemasAdapter
            setHasFixedSize(true)
        }

        viewPager = root.findViewById(R.id.viewpager)
        tabs = root.findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        progressBar = root.findViewById(R.id.progress_bar)
    }

    fun peek(movieId: Int) {
        behavior?.let {
            val finalPeekHeight = resources.getDimension(R.dimen.btn_session_rooms_height)
                .toInt()
            ValueAnimator.ofInt(0, finalPeekHeight)
                .apply {
                    duration = 1000
                    startDelay = 400
                    interpolator = FastOutSlowInInterpolator()
                    addUpdateListener { valueAnimator ->
                        it.peekHeight = valueAnimator.animatedValue as Int
                    }
                    doOnEnd {
                        viewModel.onReady(movieId)
                    }
                }
                .start()
        }
    }

    private fun toggle() {
        behavior?.let {
            when (it.state) {
                STATE_EXPANDED -> it.state = STATE_COLLAPSED
                STATE_COLLAPSED -> it.state = STATE_EXPANDED
                else -> Unit // No-op
            }
        }
    }

    private fun interpolateChildViews(offset: Float) {
        expandCollapseArrow.rotation = offset * 180f
        if (!viewPager.isGone) {
            viewPager.alpha = offset
        }
        if (!tabs.isGone) {
            tabs.alpha = offset
        }
        if (!recyclerView.isGone) {
            recyclerView.alpha = offset
        }
    }

    private fun updateViews(state: Int) {
        val isSessionsVisible = state == STATE_SCHEDULE
        TransitionManager.beginDelayedTransition(sheet)
        setVisible(recyclerView, !isSessionsVisible)
        setVisible(sheetDescription, isSessionsVisible)
        setVisible(resetArrow, isSessionsVisible)
        setVisible(viewPager, isSessionsVisible)
        setVisible(tabs, isSessionsVisible)
        tabs.scaleX = if (isSessionsVisible) {
            1f
        } else {
            0f
        }

        if (state == STATE_SCHEDULE) {
            sheetHeader.text = getString(R.string.header_sessions)
            resetArrow.setOnClickListener {
                viewModel.onBackClicked()
            }
        } else {
            sheetHeader.text = getString(R.string.header_playing_rooms)
        }
    }

    private fun setVisible(view: View, visible: Boolean) {
        view.isGone = !visible
    }

}
