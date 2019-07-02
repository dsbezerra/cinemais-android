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
import com.diegobezerra.cinemaisapp.databinding.FragmentPlayingCinemasBinding
import com.diegobezerra.cinemaisapp.ui.movie.playingcinemas.PlayingCinemasViewModel.Companion.STATE_SCHEDULE
import com.diegobezerra.cinemaisapp.ui.schedule.ScheduleAdapter
import com.diegobezerra.cinemaisapp.widget.CinemaisTabLayout
import com.diegobezerra.core.cinemais.domain.model.Schedule
import com.diegobezerra.core.event.EventObserver
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class PlayingCinemasFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val playingCinemasViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)
            .get(PlayingCinemasViewModel::class.java)
    }

    private val playingCinemasAdapter by lazy { PlayingCinemasAdapter(playingCinemasViewModel) }

    // TODO: Create custom view for this sheet
    private lateinit var scheduleContainer: ViewGroup
    private lateinit var sheet: ViewGroup
    private lateinit var sheetHeader: TextView
    private lateinit var sheetDescription: TextView
    private lateinit var resetArrow: View
    private lateinit var expandCollapseArrow: View
    private lateinit var tabs: CinemaisTabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var recyclerView: RecyclerView

    private var behavior: BottomSheetBehavior<*>? = null

    // Added to prevent adapter recreations
    private var prevSchedule: Schedule? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentPlayingCinemasBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@PlayingCinemasFragment
            viewModel = playingCinemasViewModel
        }

        scheduleContainer = binding.scheduleContainer
        sheet = binding.playingCinemasSheet
        sheetDescription = binding.sheetDescription
        sheetHeader = binding.sheetHeader
        resetArrow = binding.resetArrow
        expandCollapseArrow = binding.expandOrCollapseArrow
        recyclerView = binding.recyclerView
        viewPager = binding.viewpager
        tabs = binding.tabs
        tabs.setupWithViewPager(viewPager)

        recyclerView.run {
            adapter = playingCinemasAdapter
            setHasFixedSize(true)
        }

        val progressBar = binding.root.findViewById<View>(R.id.progress_bar)

        playingCinemasViewModel.loading.observe(this, Observer { loading ->
            setVisible(progressBar, loading)
        })

        playingCinemasViewModel.cinemas.observe(this, Observer {
            playingCinemasAdapter.data = it
            playingCinemasAdapter.notifyDataSetChanged()
        })

        playingCinemasViewModel.schedule.observe(this, Observer { schedule ->
            if (schedule != prevSchedule) {
                viewPager.adapter =
                    ScheduleAdapter(childFragmentManager, requireActivity(), schedule, true)
                prevSchedule = schedule
            }
        })

        playingCinemasViewModel.state.observe(this, Observer {
            // Make sure our views are visible just in case the last onSlide
            // set them to invisible.
            interpolateChildViews(1f)
            updateViews(it)
        })

        playingCinemasViewModel.toggleSheetAction.observe(this, EventObserver {
            toggleSheet()
        })

        return binding.root
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
                    addUpdateListener { animator ->
                        it.peekHeight = animator.animatedValue as Int
                    }
                    doOnEnd {
                        playingCinemasViewModel.onReady(movieId)
                    }
                }
                .start()
        }
    }

    private fun toggleSheet() {
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
        if (!scheduleContainer.isGone) {
            scheduleContainer.alpha = offset
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
        setVisible(scheduleContainer, isSessionsVisible)
        tabs.scaleX = if (isSessionsVisible) {
            1f
        } else {
            0f
        }
    }

    private fun setVisible(view: View, visible: Boolean) {
        view.isGone = !visible
    }

}
